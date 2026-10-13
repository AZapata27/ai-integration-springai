package com.example.openiaintegration.service;

import com.example.openiaintegration.dto.RagAnswer;
import com.example.openiaintegration.dto.RagSource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Retrieval Augmented Generation with Spring AI's {@link QuestionAnswerAdvisor}: the advisor retrieves
 * the top-K most similar documents from the vector store and appends them as context to the user
 * message. {@link EmptyContextAdvisor} runs right after it and answers {@link #NO_CONTEXT_ANSWER}
 * without calling the model when nothing was retrieved, so the model cannot make an answer up.
 */
@Service
@RequiredArgsConstructor
public class RagService {

    public static final int DEFAULT_TOP_K = 4;
    public static final int MAX_TOP_K = 10;

    public static final String NO_CONTEXT_ANSWER =
            "No encontré documentos relevantes en la base de conocimiento para responder esa pregunta.";

    static final String SYSTEM_PROMPT = """
            Eres un asistente que responde preguntas usando UNICAMENTE el contexto proporcionado.
            Si la respuesta no esta en el contexto, responde que no lo sabes; no inventes informacion.
            """;

    /**
     * User message sent to the model: {@code query} is the question and {@code question_answer_context}
     * the retrieved documents (placeholders required by {@link QuestionAnswerAdvisor}).
     */
    static final PromptTemplate USER_TEMPLATE = PromptTemplate.builder().template("""
            {query}

            CONTEXTO:
            {question_answer_context}
            """).build();

    /** Retrieval runs first; the empty-context guard runs right after it, before the model call. */
    static final int RETRIEVAL_ORDER = 0;
    static final int EMPTY_CONTEXT_ORDER = RETRIEVAL_ORDER + 1;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagAnswer ask(String question, int topK) {
        int k = Math.max(1, Math.min(topK, MAX_TOP_K));

        QuestionAnswerAdvisor retrieval = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(k).build())
                .promptTemplate(USER_TEMPLATE)
                .order(RETRIEVAL_ORDER)
                .build();

        ChatClientResponse response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(question)
                .advisors(retrieval, new EmptyContextAdvisor(NO_CONTEXT_ANSWER, EMPTY_CONTEXT_ORDER))
                .call()
                .chatClientResponse();

        List<Document> documents = EmptyContextAdvisor.retrievedDocuments(response.context());
        if (documents.isEmpty()) {
            return new RagAnswer(question, NO_CONTEXT_ANSWER, List.of());
        }

        String answer = response.chatResponse().getResult().getOutput().getText();
        List<RagSource> sources = documents.stream()
                .map(document -> new RagSource(document.getId(), document.getText()))
                .toList();

        return new RagAnswer(question, answer, sources);
    }

    /**
     * Embeds and stores the given texts. Blank entries are ignored.
     *
     * @return ids of the stored documents
     */
    public List<String> index(List<String> texts) {
        List<Document> documents = (texts == null ? List.<String>of() : texts).stream()
                .filter(text -> text != null && !text.isBlank())
                .map(text -> new Document(text.trim()))
                .toList();

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
        return documents.stream().map(Document::getId).toList();
    }
}

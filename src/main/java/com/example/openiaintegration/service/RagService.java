package com.example.openiaintegration.service;

import com.example.openiaintegration.dto.RagAnswer;
import com.example.openiaintegration.dto.RagSource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Retrieval Augmented Generation: retrieves the top-K most similar documents from the vector store
 * and injects them as context in the system message of the chat prompt.
 *
 * <p>Spring AI 0.8.1 (the version used here) has no {@code QuestionAnswerAdvisor}, so the prompt is
 * built explicitly with a {@link SystemPromptTemplate}.
 */
@Service
@RequiredArgsConstructor
public class RagService {

    public static final int DEFAULT_TOP_K = 4;
    public static final int MAX_TOP_K = 10;

    public static final String NO_CONTEXT_ANSWER =
            "No encontré documentos relevantes en la base de conocimiento para responder esa pregunta.";

    static final String DOCUMENT_SEPARATOR = "\n---\n";

    private static final String SYSTEM_TEMPLATE = """
            Eres un asistente que responde preguntas usando UNICAMENTE el contexto proporcionado.
            Si la respuesta no esta en el contexto, responde que no lo sabes; no inventes informacion.

            CONTEXTO:
            {context}
            """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagAnswer ask(String question, int topK) {
        int k = Math.max(1, Math.min(topK, MAX_TOP_K));
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder().query(question).topK(k).build());

        if (documents == null || documents.isEmpty()) {
            // Nothing to ground the answer on: do not call the model, so it cannot make one up.
            return new RagAnswer(question, NO_CONTEXT_ANSWER, List.of());
        }

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining(DOCUMENT_SEPARATOR));

        String answer = chatClient.prompt()
                .system(system -> system.text(SYSTEM_TEMPLATE).param("context", context))
                .user(question)
                .call()
                .content();
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

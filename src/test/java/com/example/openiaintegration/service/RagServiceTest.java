package com.example.openiaintegration.service;

import com.example.openiaintegration.dto.RagAnswer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static com.example.openiaintegration.support.AiTestResponses.chatResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RagServiceTest {

    private final ChatModel chatModel = mock(ChatModel.class);
    private final VectorStore vectorStore = mock(VectorStore.class);
    private final RagService ragService = new RagService(ChatClient.create(chatModel), vectorStore);

    @Test
    void retrievedDocumentsAreInjectedAsContextInThePrompt() {
        Document spring = new Document("spring-doc", "Spring AI integra modelos de lenguaje en Spring Boot", Map.of());
        Document chroma = new Document("chroma-doc", "Chroma es una base de datos vectorial", Map.of());
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(spring, chroma));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse("Spring AI integra LLMs en Spring Boot."));

        RagAnswer answer = ragService.ask("Que es Spring AI?", 2);

        ArgumentCaptor<SearchRequest> search = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(search.capture());
        assertThat(search.getValue().getQuery()).isEqualTo("Que es Spring AI?");
        assertThat(search.getValue().getTopK()).isEqualTo(2);

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(captor.capture());
        List<Message> messages = captor.getValue().getInstructions();
        assertThat(messages).hasSize(2);

        Message system = messages.get(0);
        assertThat(system.getMessageType()).isEqualTo(MessageType.SYSTEM);
        assertThat(system.getText())
                .contains("CONTEXTO:")
                .contains("Spring AI integra modelos de lenguaje en Spring Boot")
                .contains("Chroma es una base de datos vectorial");

        Message user = messages.get(1);
        assertThat(user.getMessageType()).isEqualTo(MessageType.USER);
        assertThat(user.getText()).isEqualTo("Que es Spring AI?");

        assertThat(answer.answer()).isEqualTo("Spring AI integra LLMs en Spring Boot.");
        assertThat(answer.sources()).extracting("id").containsExactly("spring-doc", "chroma-doc");
    }

    @Test
    void withoutRetrievedDocumentsTheModelIsNotCalled() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        RagAnswer answer = ragService.ask("Pregunta sin contexto", 4);

        assertThat(answer.answer()).isEqualTo(RagService.NO_CONTEXT_ANSWER);
        assertThat(answer.sources()).isEmpty();
        verify(chatModel, never()).call(any(Prompt.class));
    }

    @Test
    void topKIsClampedToASaneRange() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        ragService.ask("q", 0);
        ragService.ask("q", 500);

        ArgumentCaptor<SearchRequest> search = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore, org.mockito.Mockito.times(2)).similaritySearch(search.capture());
        assertThat(search.getAllValues()).extracting(SearchRequest::getTopK).containsExactly(1, RagService.MAX_TOP_K);
    }

    @Test
    @SuppressWarnings("unchecked")
    void indexStoresNonBlankDocumentsAndReturnsTheirIds() {
        List<String> ids = ragService.index(List.of("Primer documento", "  ", "Segundo documento"));

        ArgumentCaptor<List<Document>> stored = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(stored.capture());
        assertThat(stored.getValue()).extracting(Document::getText)
                .containsExactly("Primer documento", "Segundo documento");
        assertThat(ids).hasSize(2).doesNotContainNull();
    }

    @Test
    void indexIgnoresEmptyInput() {
        assertThat(ragService.index(List.of())).isEmpty();
        assertThat(ragService.index(null)).isEmpty();
        verify(vectorStore, never()).add(anyList());
    }
}

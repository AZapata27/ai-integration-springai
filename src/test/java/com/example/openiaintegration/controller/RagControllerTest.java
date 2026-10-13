package com.example.openiaintegration.controller;

import com.example.openiaintegration.service.IAuthorService;
import com.example.openiaintegration.service.IBookService;
import com.example.openiaintegration.service.RagService;
import com.example.openiaintegration.support.TestChatClientConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static com.example.openiaintegration.support.AiTestResponses.chatResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end through the web layer and the real {@link RagService}; only the model and the
 * vector store are mocked.
 */
@WebMvcTest(RagController.class)
@Import({RagService.class, TestChatClientConfig.class})
class RagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatModel chatModel;
    @MockitoBean
    private VectorStore vectorStore;

    // Required by the application runner that seeds data on startup
    @MockitoBean
    private IAuthorService authorService;
    @MockitoBean
    private IBookService bookService;

    @Test
    void askAnswersWithRetrievedContext() throws Exception {
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(new Document("doc-1", "El futuro es la inteligencia artificial", Map.of())));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse("La inteligencia artificial."));

        mockMvc.perform(get("/rag/ask").param("question", "Cual es el futuro?"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.question").value("Cual es el futuro?"))
                .andExpect(jsonPath("$.data.answer").value("La inteligencia artificial."))
                .andExpect(jsonPath("$.data.sources[0].id").value("doc-1"))
                .andExpect(jsonPath("$.data.sources[0].content").value("El futuro es la inteligencia artificial"));

        ArgumentCaptor<SearchRequest> search = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(search.capture());
        assertThat(search.getValue().getTopK()).isEqualTo(RagService.DEFAULT_TOP_K);

        ArgumentCaptor<Prompt> prompt = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(prompt.capture());
        assertThat(prompt.getValue().getContents()).contains("El futuro es la inteligencia artificial");
    }

    @Test
    void askWithoutMatchesReturnsNoContextAnswer() throws Exception {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        mockMvc.perform(get("/rag/ask").param("question", "Nada relacionado").param("topK", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answer").value(RagService.NO_CONTEXT_ANSWER))
                .andExpect(jsonPath("$.data.sources.length()").value(0));

        verify(chatModel, never()).call(any(Prompt.class));
    }

    @Test
    void askRejectsMissingOrBlankQuestion() throws Exception {
        mockMvc.perform(get("/rag/ask")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/rag/ask").param("question", "   ")).andExpect(status().isBadRequest());

        verifyNoInteractions(vectorStore);
        verify(chatModel, never()).call(any(Prompt.class));
    }

    @Test
    void documentsAreIndexed() throws Exception {
        mockMvc.perform(post("/rag/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documents\":[\"Spring AI es lo maximo\",\"Chroma guarda embeddings\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(vectorStore).add(anyList());
    }

    @Test
    void emptyDocumentListIsRejected() throws Exception {
        mockMvc.perform(post("/rag/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documents\":[]}"))
                .andExpect(status().isBadRequest());

        verify(vectorStore, never()).add(anyList());
    }
}

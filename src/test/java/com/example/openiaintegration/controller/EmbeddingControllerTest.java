package com.example.openiaintegration.controller;

import com.example.openiaintegration.service.IAuthorService;
import com.example.openiaintegration.service.IBookService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmbeddingController.class)
class EmbeddingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmbeddingModel embeddingModel;
    @MockitoBean
    private VectorStore vectorStore;

    @MockitoBean
    private IAuthorService authorService;
    @MockitoBean
    private IBookService bookService;

    @Test
    void generateEmbedsTheMessage() throws Exception {
        when(embeddingModel.embedForResponse(List.of("hello")))
                .thenReturn(new EmbeddingResponse(List.of(new Embedding(new float[]{0.1f, 0.2f, 0.3f}, 0))));

        mockMvc.perform(get("/embeddings/generate").param("message", "hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.embedding.results[0].output.length()").value(3));

        verify(embeddingModel).embedForResponse(List.of("hello"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void vectorStoreIndexesDocumentsAndReturnsTopMatch() throws Exception {
        Document best = new Document("Spring AI es lo maximo");
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(best));

        mockMvc.perform(get("/embeddings/vectorstore").param("message", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("Spring AI es lo maximo"));

        ArgumentCaptor<List<Document>> docs = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(docs.capture());
        assertThat(docs.getValue()).hasSize(3);

        ArgumentCaptor<SearchRequest> search = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(search.capture());
        assertThat(search.getValue().getQuery()).isEqualTo("spring");
        assertThat(search.getValue().getTopK()).isEqualTo(1);
    }
}

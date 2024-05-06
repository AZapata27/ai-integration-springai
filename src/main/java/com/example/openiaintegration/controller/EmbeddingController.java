package com.example.openiaintegration.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/embeddings")
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingClient embeddingClient;
    private final VectorStore vectorStore;

    @GetMapping("/generate")
    public Map<String, EmbeddingResponse> generateEmbeddings(@RequestParam(value = "message") String message) {

        EmbeddingResponse embeddingResponse = this.embeddingClient.embedForResponse(List.of(message));

        return Map.of("embedding", embeddingResponse);
    }

    @GetMapping("/vectorstore")
    public List<Document> useVectorStore(@RequestParam(value = "message") String message) {

        //vectorStore.delete(Arrays.asList("97a54d9d-21ad-4cee-8b1a-1937b5478197"));

        List<Document> documents = List.of(
                new Document("Spring AI es lo maximo", Map.of("meta1", "meta1")),
                new Document("Python es mas popular en la IA Deep Learning"),
                new Document("El futuro es la inteligencia artificial", Map.of("meta2", "meta2"))
        );

        vectorStore.add(documents);

        List<Document> results = vectorStore.similaritySearch(SearchRequest.query(message).withTopK(1));

        return results;
    }
}

package com.example.openiaintegration.config;

import org.springframework.ai.chroma.ChromaApi;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorsore.ChromaVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ChromaConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ChromaApi chromaApi(RestTemplate restTemplate) {
        String chromaUrl = "http://localhost:8000";
        return new ChromaApi(chromaUrl, restTemplate);
    }

    @Bean
    public VectorStore chromaVectorStore(EmbeddingClient embeddingClient, ChromaApi chromaApi) {
        return new ChromaVectorStore(embeddingClient, chromaApi, "TestCollection");
    }
}

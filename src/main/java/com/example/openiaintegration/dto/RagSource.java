package com.example.openiaintegration.dto;

/**
 * A document retrieved from the vector store and used as context for a RAG answer.
 */
public record RagSource(String id, String content) {
}

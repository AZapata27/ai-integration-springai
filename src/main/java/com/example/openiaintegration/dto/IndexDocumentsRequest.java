package com.example.openiaintegration.dto;

import java.util.List;

/**
 * Plain-text documents to embed and store in the vector store for later retrieval.
 */
public record IndexDocumentsRequest(List<String> documents) {
}

package com.example.openiaintegration.dto;

import java.util.List;

/**
 * Answer produced by the RAG flow together with the documents that were injected as context.
 * {@code sources} is empty when nothing relevant was retrieved (and the model was not called).
 */
public record RagAnswer(String question, String answer, List<RagSource> sources) {
}

package com.example.openiaintegration.dto;

import java.util.List;

/**
 * Recommendations for a conversation. {@code message} explains an empty result
 * (no history, empty catalog, or nothing usable in the model's answer).
 */
public record BookRecommendations(List<BookRecommendation> recommendations, String message) {
}

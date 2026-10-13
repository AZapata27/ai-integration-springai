package com.example.openiaintegration.dto;

/**
 * A catalog book recommended to the user, with its author and the model's reason.
 */
public record BookRecommendation(String title, String author, String reason) {
}

package com.example.openiaintegration.controller;

import com.example.openiaintegration.dto.BookRecommendations;
import com.example.openiaintegration.dto.ResponseDTO;
import com.example.openiaintegration.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * Recommends catalog books based on the user messages of a conversation.
     * {@code chatId} defaults to "1", the id used by {@code /chats/generateConversation}.
     */
    @GetMapping("/books")
    public ResponseEntity<ResponseDTO<BookRecommendations>> recommendBooks(
            @RequestParam(value = "chatId", defaultValue = "1") String chatId,
            @RequestParam(value = "limit", defaultValue = "" + RecommendationService.DEFAULT_LIMIT) int limit)
            throws Exception {

        return ResponseEntity.ok(new ResponseDTO<>(200, "success", recommendationService.recommendBooks(chatId, limit)));
    }
}

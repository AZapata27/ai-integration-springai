package com.example.openiaintegration.support;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

/**
 * Helpers to build canned Spring AI responses so tests never reach a real model.
 */
public final class AiTestResponses {

    private AiTestResponses() {
    }

    public static ChatResponse chatResponse(String content) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(content))));
    }
}

package com.example.openiaintegration.service;

import org.springframework.ai.chat.model.ChatResponse;

public interface BasicEvaluationTest {
    void evaluateQuestionAndAnswer(String question, ChatResponse response, boolean factBased);
}

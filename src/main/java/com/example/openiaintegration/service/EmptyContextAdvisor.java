package com.example.openiaintegration.service;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.document.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Short-circuits the advisor chain when the {@link QuestionAnswerAdvisor} that ran before it retrieved
 * no documents: it answers with a fixed message and never calls the model.
 * Must be ordered after the {@link QuestionAnswerAdvisor}.
 */
public class EmptyContextAdvisor implements CallAdvisor {

    private final String answer;
    private final int order;

    public EmptyContextAdvisor(String answer, int order) {
        this.answer = answer;
        this.order = order;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        if (!retrievedDocuments(request.context()).isEmpty()) {
            return chain.nextCall(request);
        }
        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage(answer))));
        return new ChatClientResponse(response, new HashMap<>(request.context()));
    }

    /**
     * Documents stored by {@link QuestionAnswerAdvisor} in the request/response context; empty if none.
     */
    static List<Document> retrievedDocuments(Map<String, Object> context) {
        Object documents = context == null ? null : context.get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        if (documents instanceof List<?> list) {
            return list.stream()
                    .filter(Document.class::isInstance)
                    .map(Document.class::cast)
                    .toList();
        }
        return List.of();
    }

    @Override
    public String getName() {
        return EmptyContextAdvisor.class.getSimpleName();
    }

    @Override
    public int getOrder() {
        return order;
    }
}

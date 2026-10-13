package com.example.openiaintegration.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static com.example.openiaintegration.support.AiTestResponses.chatResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The LLM-as-a-judge evaluator: builds an evaluation prompt from the .st templates and
 * asks the model for a second opinion only when the first verdict is "no".
 */
class BasicEvaluationTestBasicImplTest {

    private ChatModel chatModel;
    private BasicEvaluationTestBasicImpl evaluator;

    @BeforeEach
    void setUp() {
        chatModel = mock(ChatModel.class);
        evaluator = new BasicEvaluationTestBasicImpl(ChatClient.create(chatModel));
        evaluator.qaEvaluatorAccurateAnswerResource = new ClassPathResource("prompts/evaluation/qa-evaluator-accurate-answer.st");
        evaluator.qaEvaluatorNotRelatedResource = new ClassPathResource("prompts/evaluation/qa-evaluator-not-related-message.st");
        evaluator.qaEvaluatorFactBasedAnswerResource = new ClassPathResource("prompts/evaluation/qa-evaluator-fact-based-answer.st");
        evaluator.userEvaluatorResource = new ClassPathResource("prompts/evaluation/user-evaluator-message.st");
    }

    @Test
    void relatedAnswerNeedsASingleModelCall() {
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse("YES"));

        evaluator.evaluateQuestionAndAnswer("Capital of France?", chatResponse("Paris"), false);

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(1)).call(captor.capture());

        List<Message> messages = captor.getValue().getInstructions();
        assertThat(messages).extracting(Message::getMessageType)
                .containsExactly(MessageType.USER, MessageType.SYSTEM);
        assertThat(messages.get(0).getText()).contains("Capital of France?").contains("Paris");
        assertThat(messages.get(1).getText()).isEqualTo(contentOf(evaluator.qaEvaluatorAccurateAnswerResource));
    }

    @Test
    void factBasedEvaluationUsesFactBasedSystemPrompt() {
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse("yes"));

        evaluator.evaluateQuestionAndAnswer("Who wrote it?", chatResponse("Vargas Llosa"), true);

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(captor.capture());
        assertThat(captor.getValue().getInstructions().get(1).getText())
                .isEqualTo(contentOf(evaluator.qaEvaluatorFactBasedAnswerResource));
    }

    @Test
    void unrelatedAnswerAsksTheModelForTheReason() {
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(chatResponse("no"))
                .thenReturn(chatResponse("The answer talks about something else"));

        evaluator.evaluateQuestionAndAnswer("Capital of France?", chatResponse("Bananas"), false);

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(2)).call(captor.capture());
        Prompt followUp = captor.getAllValues().get(1);
        assertThat(followUp.getInstructions().get(1).getText())
                .isEqualTo(contentOf(evaluator.qaEvaluatorNotRelatedResource));
    }

    private static String contentOf(org.springframework.core.io.Resource resource) {
        try {
            return resource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

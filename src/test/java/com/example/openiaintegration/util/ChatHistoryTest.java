package com.example.openiaintegration.util;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ChatHistoryTest {

    private final ChatHistory history = new ChatHistory();

    private static AssistantMessage assistantChunk(String text, Map<String, Object> properties) {
        return AssistantMessage.builder().content(text).properties(properties).build();
    }

    @Test
    void unknownChatHasEmptyHistory() {
        assertThat(history.getAll("missing")).isEmpty();
    }

    @Test
    void userMessagesAreCommittedImmediatelyPerChat() {
        history.addMessage("a", new UserMessage("hello"));
        history.addMessage("b", new UserMessage("other chat"));
        history.addMessage("a", new UserMessage("again"));

        assertThat(history.getAll("a")).extracting(Message::getText).containsExactly("hello", "again");
        assertThat(history.getAll("b")).extracting(Message::getText).containsExactly("other chat");
    }

    @Test
    void streamedAssistantChunksAreAggregatedUntilStop() {
        history.addMessage("c", assistantChunk("Hel", Map.of("id", "m1")));
        history.addMessage("c", assistantChunk("lo", Map.of("id", "m1")));

        assertThat(history.getAll("c")).isEmpty();

        history.addMessage("c", assistantChunk("!", Map.of("id", "m1", "finishReason", "STOP")));

        assertThat(history.getAll("c")).singleElement()
                .extracting(Message::getText).isEqualTo("Hello!");
    }

    @Test
    void getLastNReturnsTheMostRecentMessages() {
        for (int i = 1; i <= 5; i++) {
            history.addMessage("d", new UserMessage("m" + i));
        }

        assertThat(history.getLastN("d", 2)).extracting(Message::getText).containsExactly("m4", "m5");
        assertThat(history.getLastN("d", 10)).hasSize(5);
    }
}

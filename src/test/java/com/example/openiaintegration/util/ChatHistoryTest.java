package com.example.openiaintegration.util;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ChatHistoryTest {

    private final ChatHistory history = new ChatHistory();

    @Test
    void unknownChatHasEmptyHistory() {
        assertThat(history.getAll("missing")).isEmpty();
    }

    @Test
    void userMessagesAreCommittedImmediatelyPerChat() {
        history.addMessage("a", new UserMessage("hello"));
        history.addMessage("b", new UserMessage("other chat"));
        history.addMessage("a", new UserMessage("again"));

        assertThat(history.getAll("a")).extracting(Message::getContent).containsExactly("hello", "again");
        assertThat(history.getAll("b")).extracting(Message::getContent).containsExactly("other chat");
    }

    @Test
    void streamedAssistantChunksAreAggregatedUntilStop() {
        history.addMessage("c", new AssistantMessage("Hel", Map.of("id", "m1")));
        history.addMessage("c", new AssistantMessage("lo", Map.of("id", "m1")));

        assertThat(history.getAll("c")).isEmpty();

        history.addMessage("c", new AssistantMessage("!", Map.of("id", "m1", "finishReason", "STOP")));

        assertThat(history.getAll("c")).singleElement()
                .extracting(Message::getContent).isEqualTo("Hello!");
    }

    @Test
    void getLastNReturnsTheMostRecentMessages() {
        for (int i = 1; i <= 5; i++) {
            history.addMessage("d", new UserMessage("m" + i));
        }

        assertThat(history.getLastN("d", 2)).extracting(Message::getContent).containsExactly("m4", "m5");
        assertThat(history.getLastN("d", 10)).hasSize(5);
    }
}

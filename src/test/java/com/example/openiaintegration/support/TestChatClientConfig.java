package com.example.openiaintegration.support;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Builds the real fluent {@link ChatClient} on top of the (mocked) {@link ChatModel} of the test,
 * so controllers and services exercise the actual ChatClient pipeline (templates, advisors,
 * structured output) while the model itself never leaves the JVM.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestChatClientConfig {

    @Bean
    ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}

package com.example.openiaintegration.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes a single {@link ChatClient} built from the auto-configured {@link ChatClient.Builder}.
 *
 * <p>Model, temperature and max tokens are default options of the OpenAI chat model and live in
 * {@code application.yml} ({@code spring.ai.openai.chat.options.*}).
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}

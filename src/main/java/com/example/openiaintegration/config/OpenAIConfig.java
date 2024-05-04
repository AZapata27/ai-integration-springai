package com.example.openiaintegration.config;

import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    //@Value("${}")
    @Value("${spring.ai.openai.api-key}")
    private String API_KEY;

    @Bean
    public OpenAiChatClient openAiChatClient() {
        final OpenAiApi openAiApi = new OpenAiApi(API_KEY);

        return new OpenAiChatClient(openAiApi, OpenAiChatOptions.builder()
                .withModel("gpt-3.5-turbo")
                .withTemperature(1f)
                .withMaxTokens(200)
                .build()
        );
    }
}

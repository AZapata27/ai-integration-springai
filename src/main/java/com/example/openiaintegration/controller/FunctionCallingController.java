package com.example.openiaintegration.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tool calling: each request enables a tool registered in {@code FunctionConfig} by its name.
 */
@RestController
@RequestMapping("/functions")
@RequiredArgsConstructor
public class FunctionCallingController {

    private final ChatClient chatClient;

    @GetMapping
    public ResponseEntity<?> getWeather() {
        String content = chatClient.prompt()
                .user("What's the weather like in San Francisco, Tokyo, and Paris?")
                .options(OpenAiChatOptions.builder().toolNames("WeatherInfo").build())
                .call()
                .content();

        return ResponseEntity.ok(content);
    }

    @GetMapping("/book")
    public ResponseEntity<?> getBookInfo(@RequestParam("bookName") String bookName) {
        String content = chatClient.prompt()
                .user("Dame la informacion de este libro " + bookName + "?")
                .options(OpenAiChatOptions.builder().toolNames("BookInfo").build())
                .call()
                .content();

        return ResponseEntity.ok(content);
    }
}

package com.example.openiaintegration.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/functions")
@RequiredArgsConstructor
public class FunctionCallingController {

    private final ChatClient chatClient;

    @GetMapping
    public ResponseEntity<?> getWeather() {
        UserMessage userMessage = new UserMessage("What's the weather like in San Francisco, Tokyo, and Paris?");

        ChatResponse response = chatClient.call(new Prompt(List.of(userMessage),
                OpenAiChatOptions.builder().withFunction("WeatherInfo").build()
                ));

        return ResponseEntity.ok(response.getResult().getOutput().getContent());
    }

    @GetMapping("/book")
    public ResponseEntity<?> getBookInfo(@RequestParam("bookName") String bookName) {
        UserMessage userMessage = new UserMessage("Dame la informacion de este libro " + bookName + "?" );

        ChatResponse response = chatClient.call(new Prompt(List.of(userMessage),
                OpenAiChatOptions.builder().withFunction("BookInfo").build()
        ));

        return ResponseEntity.ok(response.getResult().getOutput().getContent());
    }
}

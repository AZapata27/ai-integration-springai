package com.example.openiaintegration.controller;

import com.example.openiaintegration.dto.AuthorBook;
import com.example.openiaintegration.dto.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatClient chatClient;

    @GetMapping("/generate")
    public ResponseEntity<ResponseDTO> generateText(@RequestParam(value = "message") String message) {

        ChatResponse chatResponse = chatClient.call(new Prompt(message));
        String result = chatResponse.getResult().getOutput().getContent();

        return ResponseEntity.ok(new ResponseDTO(200,"sucess",result));

    }

    @GetMapping("/generate/output")
    public ResponseEntity<AuthorBook> generateOutputParser(@RequestParam(value = "author") String author) {

        BeanOutputParser<AuthorBook> authorBookBeanOutputParser = new BeanOutputParser<>(AuthorBook.class);

        String template = """
                Dime los titulos de los libros escritos por este author {author}. {format}
                
                """;

        PromptTemplate promptTemplate =  new PromptTemplate(template, Map.of("author", author,"format", authorBookBeanOutputParser.getFormat()));

        Prompt prompt = promptTemplate.create(Map.of("author", author));

        ChatResponse chatResponse = chatClient.call(prompt);

        Generation generation = chatResponse.getResult();

        AuthorBook response = authorBookBeanOutputParser.parse(generation.getOutput().getContent());



        return ResponseEntity.ok(response);

    }
}

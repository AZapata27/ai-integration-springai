package com.example.openiaintegration.controller;

import com.example.openiaintegration.dto.AuthorBook;
import com.example.openiaintegration.dto.BookInfoDTO;
import com.example.openiaintegration.dto.ResponseDTO;
import com.example.openiaintegration.util.ChatHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.ChatMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatClient chatClient;
    private final ChatHistory chatHistory;

    @GetMapping("/generate")
    public ResponseEntity<ResponseDTO<String>> generateText(@RequestParam(value = "message") String message) {
        ChatResponse chatResponse = chatClient.call(new Prompt(message));
        String result = chatResponse.getResult().getOutput().getContent();

        return ResponseEntity.ok(new ResponseDTO<>(200, "success", result));
    }

    @GetMapping("/generate/prompt")
    public ResponseEntity<ResponseDTO<String>> generateTextWithPrompt(
            @RequestParam(value = "param") String param,
            @RequestParam(value = "topic") String topic) {

        PromptTemplate promptTemplate = new PromptTemplate("Tell me about {param} and {topic}");
        Prompt prompt = promptTemplate.create(Map.of("param", param, "topic", topic));

        ChatResponse chatResponse = chatClient.call(prompt);
        String result = chatResponse.getResult().getOutput().getContent();

        return ResponseEntity.ok(new ResponseDTO<>(200, "success", result));
    }

    @GetMapping("/generate/output")
    public ResponseEntity<AuthorBook> generateOutputParser(
            @RequestParam(value = "author") String author){

        BeanOutputParser<AuthorBook> outputParser = new BeanOutputParser<>(AuthorBook.class);

        String template = """
                Tell me book titles of {author}. {format}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(template, Map.of("author", author, "format", outputParser.getFormat()));
        Prompt prompt = promptTemplate.create(Map.of("author", author));

        ChatResponse chatResponse = chatClient.call(prompt);
        Generation generation = chatResponse.getResult();
        AuthorBook authorBook = outputParser.parse(generation.getOutput().getContent());

        return ResponseEntity.ok(authorBook);
    }

    @GetMapping("/generate/book/output")
    public ResponseEntity<BookInfoDTO> generateOutputParserBook(
            @RequestParam(value = "name") String bookName){

        BeanOutputParser<BookInfoDTO> outputParser = new BeanOutputParser<>(BookInfoDTO.class);

        String template = """
                Tell me the author of this book which name is {bookName}. {format}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(template, Map.of("bookName", bookName, "format", outputParser.getFormat() ));
        Prompt prompt = promptTemplate.create(Map.of("bookName", bookName));

        ChatResponse chatResponse = chatClient.call(prompt);
        Generation generation = chatResponse.getResult();
        BookInfoDTO bookInfoDTO = outputParser.parse(generation.getOutput().getContent());

        return ResponseEntity.ok(bookInfoDTO);
    }


    @GetMapping("/generateConversation")
    public ResponseEntity<ResponseDTO<String>> generateConversation(
            @RequestParam(value = "message") String message){

        chatHistory.addMessage("1", new ChatMessage(MessageType.USER, message));

        ChatResponse chatResponse = chatClient.call(new Prompt(chatHistory.getAll("1")));
        String result = chatResponse.getResult().getOutput().getContent();

        return ResponseEntity.ok(new ResponseDTO<>(200, "success", result));
    }

     /*@GetMapping("/ollama")
    public ResponseEntity<String> testOllama(@RequestParam("text") String text){
        ChatResponse response = chatClient.call(new Prompt(text, OllamaOptions.create().withModel("codellama:7b")));

        String content = response.getResult().getOutput().getContent();

        return ResponseEntity.ok(content);
    }*/
}

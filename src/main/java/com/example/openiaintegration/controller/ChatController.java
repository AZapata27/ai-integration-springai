package com.example.openiaintegration.controller;

import com.example.openiaintegration.dto.AuthorBook;
import com.example.openiaintegration.dto.BookInfoDTO;
import com.example.openiaintegration.dto.ResponseDTO;
import com.example.openiaintegration.util.ChatHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatClient chatClient;
    private final ChatHistory chatHistory;

    @GetMapping("/generate")
    public ResponseEntity<ResponseDTO<String>> generateText(@RequestParam(value = "message") String message) {
        String result = chatClient.prompt()
                .user(message)
                .call()
                .content();

        return ResponseEntity.ok(new ResponseDTO<>(200, "success", result));
    }

    @GetMapping("/generate/prompt")
    public ResponseEntity<ResponseDTO<String>> generateTextWithPrompt(
            @RequestParam(value = "param") String param,
            @RequestParam(value = "topic") String topic) {

        String result = chatClient.prompt()
                .user(user -> user.text("Tell me about {param} and {topic}")
                        .param("param", param)
                        .param("topic", topic))
                .call()
                .content();

        return ResponseEntity.ok(new ResponseDTO<>(200, "success", result));
    }

    /**
     * Structured output: {@code entity(...)} appends the JSON schema of {@link AuthorBook} to the prompt
     * and converts the model answer into the record.
     */
    @GetMapping("/generate/output")
    public ResponseEntity<AuthorBook> generateOutputParser(
            @RequestParam(value = "author") String author){

        AuthorBook authorBook = chatClient.prompt()
                .user(user -> user.text("Tell me book titles of {author}.").param("author", author))
                .call()
                .entity(AuthorBook.class);

        return ResponseEntity.ok(authorBook);
    }

    @GetMapping("/generate/book/output")
    public ResponseEntity<BookInfoDTO> generateOutputParserBook(
            @RequestParam(value = "name") String bookName){

        BookInfoDTO bookInfoDTO = chatClient.prompt()
                .user(user -> user.text("Tell me the author of this book which name is {bookName}.")
                        .param("bookName", bookName))
                .call()
                .entity(BookInfoDTO.class);

        return ResponseEntity.ok(bookInfoDTO);
    }


    @GetMapping("/generateConversation")
    public ResponseEntity<ResponseDTO<String>> generateConversation(
            @RequestParam(value = "message") String message){

        chatHistory.addMessage("1", new UserMessage(message));

        String result = chatClient.prompt()
                .messages(chatHistory.getAll("1"))
                .call()
                .content();

        return ResponseEntity.ok(new ResponseDTO<>(200, "success", result));
    }

     /*@GetMapping("/ollama")
    public ResponseEntity<String> testOllama(@RequestParam("text") String text){
        String content = chatClient.prompt()
                .user(text)
                .options(OllamaOptions.builder().model("codellama:7b").build())
                .call()
                .content();

        return ResponseEntity.ok(content);
    }*/
}

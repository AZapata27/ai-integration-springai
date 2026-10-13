package com.example.openiaintegration.controller;

import com.example.openiaintegration.dto.IndexDocumentsRequest;
import com.example.openiaintegration.dto.RagAnswer;
import com.example.openiaintegration.dto.ResponseDTO;
import com.example.openiaintegration.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RagController {

    private final RagService ragService;

    /**
     * Answers a question using the top-K documents retrieved from the vector store as context.
     */
    @GetMapping("/ask")
    public ResponseEntity<ResponseDTO<RagAnswer>> ask(
            @RequestParam("question") String question,
            @RequestParam(value = "topK", defaultValue = "" + RagService.DEFAULT_TOP_K) int topK) {

        if (question.isBlank()) {
            return ResponseEntity.badRequest().body(new ResponseDTO<>(400, "question must not be blank", null));
        }
        return ResponseEntity.ok(new ResponseDTO<>(200, "success", ragService.ask(question.trim(), topK)));
    }

    /**
     * Embeds and stores plain-text documents so they can be retrieved by {@code /rag/ask}.
     */
    @PostMapping("/documents")
    public ResponseEntity<ResponseDTO<List<String>>> index(@RequestBody IndexDocumentsRequest request) {
        List<String> ids = ragService.index(request.documents());
        if (ids.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseDTO<>(400, "documents must not be empty", List.of()));
        }
        return ResponseEntity.ok(new ResponseDTO<>(200, "success", ids));
    }
}

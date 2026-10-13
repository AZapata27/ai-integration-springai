package com.example.openiaintegration.service;

import com.example.openiaintegration.dto.BookRecommendation;
import com.example.openiaintegration.dto.BookRecommendations;
import com.example.openiaintegration.model.Author;
import com.example.openiaintegration.model.Book;
import com.example.openiaintegration.util.ChatHistory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Recommends books from the stored catalog based on what the user said in a conversation
 * kept in {@link ChatHistory}. The model only picks among catalog titles; anything it returns
 * that is not in the catalog is discarded.
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    public static final int HISTORY_WINDOW = 10;
    public static final int DEFAULT_LIMIT = 3;
    public static final int MAX_LIMIT = 10;

    private static final String TEMPLATE = """
            Eres un recomendador de libros. A partir de los intereses que el usuario expreso en su conversacion,
            elige hasta {limit} libros del CATALOGO (solo de esa lista y con el titulo exacto)
            y explica brevemente por que cada uno encaja con sus intereses.

            CONVERSACION DEL USUARIO:
            {history}

            CATALOGO:
            {catalog}

            {format}
            """;

    public record ModelRecommendation(String title, String reason) {
    }

    public record ModelRecommendations(List<ModelRecommendation> recommendations) {
    }

    private final ChatClient chatClient;
    private final ChatHistory chatHistory;
    private final IBookService bookService;

    public BookRecommendations recommendBooks(String chatId, int limit) throws Exception {
        int max = Math.max(1, Math.min(limit, MAX_LIMIT));

        List<String> userMessages = chatHistory.getLastN(chatId, HISTORY_WINDOW).stream()
                .filter(message -> message.getMessageType() == MessageType.USER)
                .map(Message::getText)
                .toList();
        if (userMessages.isEmpty()) {
            return new BookRecommendations(List.of(),
                    "No hay historial de conversacion para el chat " + chatId + ".");
        }

        Map<String, Book> catalog = new LinkedHashMap<>();
        for (Book book : bookService.findAll()) {
            if (book.isEnabled() && book.getName() != null) {
                catalog.putIfAbsent(normalize(book.getName()), book);
            }
        }
        if (catalog.isEmpty()) {
            return new BookRecommendations(List.of(), "El catalogo no tiene libros disponibles.");
        }

        BeanOutputConverter<ModelRecommendations> converter = new BeanOutputConverter<>(ModelRecommendations.class);
        Prompt prompt = PromptTemplate.builder().template(TEMPLATE).build().create(Map.of(
                "limit", max,
                "history", userMessages.stream().map(m -> "- " + m).collect(Collectors.joining("\n")),
                "catalog", catalog.values().stream().map(RecommendationService::describe).collect(Collectors.joining("\n")),
                "format", converter.getFormat()));

        String content = chatClient.prompt(prompt).call().content();

        ModelRecommendations parsed;
        try {
            parsed = converter.convert(content);
        } catch (RuntimeException e) {
            logger.warn("Could not parse recommendations from the model: {}", e.getMessage());
            return new BookRecommendations(List.of(), "El modelo devolvio una respuesta que no se pudo interpretar.");
        }

        List<BookRecommendation> result = new ArrayList<>();
        if (parsed != null && parsed.recommendations() != null) {
            for (ModelRecommendation item : parsed.recommendations()) {
                Book book = item == null || item.title() == null ? null : catalog.get(normalize(item.title()));
                if (book == null || result.size() >= max) {
                    continue;
                }
                boolean duplicate = result.stream().anyMatch(r -> r.title().equals(book.getName()));
                if (!duplicate) {
                    result.add(new BookRecommendation(book.getName(), authorName(book.getAuthor()), item.reason()));
                }
            }
        }

        return new BookRecommendations(result,
                result.isEmpty() ? "El modelo no recomendo ningun libro del catalogo." : "success");
    }

    private static String describe(Book book) {
        String author = authorName(book.getAuthor());
        return "- " + book.getName() + (author == null ? "" : " (" + author + ")");
    }

    private static String authorName(Author author) {
        if (author == null || (author.getFirstName() == null && author.getLastName() == null)) {
            return null;
        }
        String first = author.getFirstName() == null ? "" : author.getFirstName();
        String last = author.getLastName() == null ? "" : author.getLastName();
        return (first + " " + last).trim();
    }

    private static String normalize(String title) {
        return title.trim().toLowerCase(Locale.ROOT);
    }
}

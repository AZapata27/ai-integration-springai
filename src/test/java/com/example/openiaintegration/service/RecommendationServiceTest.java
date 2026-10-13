package com.example.openiaintegration.service;

import com.example.openiaintegration.dto.BookRecommendations;
import com.example.openiaintegration.model.Author;
import com.example.openiaintegration.model.Book;
import com.example.openiaintegration.util.ChatHistory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static com.example.openiaintegration.support.AiTestResponses.chatResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RecommendationServiceTest {

    private final ChatModel chatModel = mock(ChatModel.class);
    private final IBookService bookService = mock(IBookService.class);
    private final ChatHistory chatHistory = new ChatHistory();
    private final RecommendationService service = new RecommendationService(ChatClient.create(chatModel), chatHistory, bookService);

    private static Book book(String name, String firstName, String lastName, boolean enabled) {
        Author author = new Author(1, firstName, lastName, "PE", "url");
        return new Book(0, name, "review", author, "cover", enabled);
    }

    @Test
    void recommendsOnlyCatalogBooksUsingTheConversationAndCatalogInThePrompt() throws Exception {
        chatHistory.addMessage("7", new UserMessage("Me gusta el realismo magico"));
        when(bookService.findAll()).thenReturn(List.of(
                book("Cien anos de soledad", "Gabriel", "Garcia Marquez", true),
                book("La ciudad y los perros", "Mario", "Vargas Llosa", true),
                book("Libro deshabilitado", "X", "Y", false)));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse("""
                {"recommendations":[
                  {"title":"cien anos de soledad","reason":"Es el clasico del realismo magico"},
                  {"title":"Un libro que no existe","reason":"inventado"}
                ]}"""));

        BookRecommendations result = service.recommendBooks("7", 3);

        assertThat(result.message()).isEqualTo("success");
        assertThat(result.recommendations()).hasSize(1);
        assertThat(result.recommendations().get(0).title()).isEqualTo("Cien anos de soledad");
        assertThat(result.recommendations().get(0).author()).isEqualTo("Gabriel Garcia Marquez");
        assertThat(result.recommendations().get(0).reason()).isEqualTo("Es el clasico del realismo magico");

        ArgumentCaptor<Prompt> prompt = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(prompt.capture());
        assertThat(prompt.getValue().getContents())
                .contains("Me gusta el realismo magico")
                .contains("Cien anos de soledad (Gabriel Garcia Marquez)")
                .contains("La ciudad y los perros (Mario Vargas Llosa)")
                .doesNotContain("Libro deshabilitado");
    }

    @Test
    void respectsTheLimit() throws Exception {
        chatHistory.addMessage("8", new UserMessage("novelas latinoamericanas"));
        when(bookService.findAll()).thenReturn(List.of(
                book("A", "a", "a", true), book("B", "b", "b", true)));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse(
                "{\"recommendations\":[{\"title\":\"A\",\"reason\":\"r\"},{\"title\":\"B\",\"reason\":\"r\"}]}"));

        assertThat(service.recommendBooks("8", 1).recommendations()).hasSize(1);
    }

    @Test
    void withoutHistoryTheModelIsNotCalled() throws Exception {
        BookRecommendations result = service.recommendBooks("sin-historial", 3);

        assertThat(result.recommendations()).isEmpty();
        assertThat(result.message()).contains("No hay historial");
        verifyNoInteractions(bookService);
        verify(chatModel, never()).call(any(Prompt.class));
    }

    @Test
    void withEmptyCatalogTheModelIsNotCalled() throws Exception {
        chatHistory.addMessage("9", new UserMessage("algo"));
        when(bookService.findAll()).thenReturn(List.of());

        BookRecommendations result = service.recommendBooks("9", 3);

        assertThat(result.recommendations()).isEmpty();
        assertThat(result.message()).contains("catalogo");
        verify(chatModel, never()).call(any(Prompt.class));
    }

    @Test
    void unparseableModelAnswerYieldsEmptyResult() throws Exception {
        chatHistory.addMessage("10", new UserMessage("algo"));
        when(bookService.findAll()).thenReturn(List.of(book("A", "a", "a", true)));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse("no es json"));

        BookRecommendations result = service.recommendBooks("10", 3);

        assertThat(result.recommendations()).isEmpty();
        assertThat(result.message()).contains("no se pudo interpretar");
    }
}

package com.example.openiaintegration.controller;

import com.example.openiaintegration.dto.BookRecommendation;
import com.example.openiaintegration.dto.BookRecommendations;
import com.example.openiaintegration.service.IAuthorService;
import com.example.openiaintegration.service.IBookService;
import com.example.openiaintegration.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;

    // Required by the application runner that seeds data on startup
    @MockBean
    private IAuthorService authorService;
    @MockBean
    private IBookService bookService;

    @Test
    void booksUsesDefaultConversationAndLimit() throws Exception {
        when(recommendationService.recommendBooks("1", RecommendationService.DEFAULT_LIMIT)).thenReturn(
                new BookRecommendations(List.of(new BookRecommendation("Cien anos de soledad", "Gabriel Garcia Marquez", "r")),
                        "success"));

        mockMvc.perform(get("/recommendations/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendations[0].title").value("Cien anos de soledad"))
                .andExpect(jsonPath("$.data.recommendations[0].author").value("Gabriel Garcia Marquez"));

        verify(recommendationService).recommendBooks("1", RecommendationService.DEFAULT_LIMIT);
    }

    @Test
    void booksAcceptsChatIdAndLimit() throws Exception {
        when(recommendationService.recommendBooks("42", 5)).thenReturn(new BookRecommendations(List.of(), "empty"));

        mockMvc.perform(get("/recommendations/books").param("chatId", "42").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("empty"));

        verify(recommendationService).recommendBooks("42", 5);
    }
}

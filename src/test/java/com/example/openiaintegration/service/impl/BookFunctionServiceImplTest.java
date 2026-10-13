package com.example.openiaintegration.service.impl;

import com.example.openiaintegration.config.FunctionConfig;
import com.example.openiaintegration.model.Book;
import com.example.openiaintegration.repo.IBookRepo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookFunctionServiceImplTest {

    private final IBookRepo repo = mock(IBookRepo.class);
    private final BookFunctionServiceImpl function = new BookFunctionServiceImpl(repo);

    @Test
    void returnsReviewOfTheRequestedBook() {
        Book book = new Book();
        book.setReview("Gran novela");
        when(repo.findByName("Conversacion en La Catedral")).thenReturn(List.of(book));

        BookFunctionServiceImpl.Response response =
                function.apply(new BookFunctionServiceImpl.Request("Conversacion en La Catedral"));

        assertThat(response.found()).isTrue();
        assertThat(response.review()).isEqualTo("Gran novela");
    }

    @Test
    void returnsNotFoundInsteadOfFailingWhenTheBookDoesNotExist() {
        when(repo.findByName("Libro inexistente")).thenReturn(List.of());

        BookFunctionServiceImpl.Response response =
                function.apply(new BookFunctionServiceImpl.Request("Libro inexistente"));

        assertThat(response.found()).isFalse();
        assertThat(response.review()).contains("No se encontró").contains("Libro inexistente");
    }

    @Test
    void usesTheFirstMatchWhenSeveralBooksShareTheName() {
        Book first = new Book();
        first.setReview("Primera");
        Book second = new Book();
        second.setReview("Segunda");
        when(repo.findByName("Duplicado")).thenReturn(List.of(first, second));

        assertThat(function.apply(new BookFunctionServiceImpl.Request("Duplicado")).review()).isEqualTo("Primera");
    }

    @Test
    void blankBookNameIsReportedWithoutQueryingTheRepository() {
        BookFunctionServiceImpl.Response response = function.apply(new BookFunctionServiceImpl.Request("  "));

        assertThat(response.found()).isFalse();
        verify(repo, never()).findByName(anyString());
    }

    @Test
    void functionCallbackReturnsTheNotFoundMessageToTheModel() {
        when(repo.findByName("Libro inexistente")).thenReturn(List.of());

        String result = new FunctionConfig().bookInfoFunction(repo).call("{\"bookName\":\"Libro inexistente\"}");

        assertThat(result).isEqualTo("No se encontró ningún libro con el nombre 'Libro inexistente'.");
    }
}

package com.example.openiaintegration.service.impl;

import com.example.openiaintegration.model.Book;
import com.example.openiaintegration.repo.IBookRepo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookFunctionServiceImplTest {

    private final IBookRepo repo = mock(IBookRepo.class);
    private final BookFunctionServiceImpl function = new BookFunctionServiceImpl(repo);

    @Test
    void returnsReviewOfTheRequestedBook() {
        Book book = new Book();
        book.setReview("Gran novela");
        when(repo.findOneByName("Conversacion en La Catedral")).thenReturn(book);

        BookFunctionServiceImpl.Response response =
                function.apply(new BookFunctionServiceImpl.Request("Conversacion en La Catedral"));

        assertThat(response.review()).isEqualTo("Gran novela");
    }
}

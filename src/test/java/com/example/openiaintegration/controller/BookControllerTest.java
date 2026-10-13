package com.example.openiaintegration.controller;

import com.example.openiaintegration.model.Author;
import com.example.openiaintegration.model.Book;
import com.example.openiaintegration.service.IAuthorService;
import com.example.openiaintegration.service.IBookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    private static final String BOOK_JSON = """
            {"name":"La ciudad y los perros","review":"Novela","urlCover":"http://cover","enabled":true,
             "author":{"idAuthor":1,"firstName":"Mario","lastName":"Vargas Llosa","country":"Peru","urlPhoto":"http://photo"}}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IBookService bookService;
    @MockBean
    private IAuthorService authorService;

    @Test
    void findAllReturnsBooks() throws Exception {
        when(bookService.findAll()).thenReturn(List.of(book(1), book(2)));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].author.lastName").value("Vargas Llosa"));
    }

    @Test
    void findByIdReturnsBook() throws Exception {
        when(bookService.findById(5)).thenReturn(book(5));

        mockMvc.perform(get("/books/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idBook").value(5));
    }

    @Test
    void saveReturnsCreatedWithLocation() throws Exception {
        when(bookService.save(any(Book.class))).thenReturn(book(9));

        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON).content(BOOK_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost:8080/books/9"))
                .andExpect(jsonPath("$.name").value("La ciudad y los perros"));
    }

    @Test
    void updateDelegatesToService() throws Exception {
        when(bookService.update(any(Book.class), eq(3))).thenReturn(book(3));

        mockMvc.perform(put("/books/3").contentType(MediaType.APPLICATION_JSON).content(BOOK_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idBook").value(3));
    }

    @Test
    void deleteDelegatesToService() throws Exception {
        mockMvc.perform(delete("/books/4")).andExpect(status().isOk());

        verify(bookService).delete(4);
    }

    private static Book book(int id) {
        Author author = new Author(1, "Mario", "Vargas Llosa", "Peru", "http://photo");
        return new Book(id, "La ciudad y los perros", "Novela", author, "http://cover", true);
    }
}

package com.example.openiaintegration.controller;

import com.example.openiaintegration.model.Author;
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

@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

    private static final String AUTHOR_JSON = """
            {"firstName":"Gabriel","lastName":"Garcia Marquez","country":"Colombia","urlPhoto":"http://photo"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAuthorService authorService;
    @MockBean
    private IBookService bookService;

    @Test
    void findAllReturnsAuthors() throws Exception {
        when(authorService.findAll()).thenReturn(List.of(author(1), author(2)));

        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].idAuthor").value(2));
    }

    @Test
    void findByIdReturnsAuthor() throws Exception {
        when(authorService.findById(7)).thenReturn(author(7));

        mockMvc.perform(get("/authors/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("Colombia"));
    }

    @Test
    void saveReturnsCreatedWithLocation() throws Exception {
        when(authorService.save(any(Author.class))).thenReturn(author(11));

        mockMvc.perform(post("/authors").contentType(MediaType.APPLICATION_JSON).content(AUTHOR_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost:8080/authors/11"))
                .andExpect(jsonPath("$.firstName").value("Gabriel"));
    }

    @Test
    void updateDelegatesToService() throws Exception {
        when(authorService.update(any(Author.class), eq(2))).thenReturn(author(2));

        mockMvc.perform(put("/authors/2").contentType(MediaType.APPLICATION_JSON).content(AUTHOR_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAuthor").value(2));
    }

    @Test
    void deleteDelegatesToService() throws Exception {
        mockMvc.perform(delete("/authors/3")).andExpect(status().isOk());

        verify(authorService).delete(3);
    }

    private static Author author(int id) {
        return new Author(id, "Gabriel", "Garcia Marquez", "Colombia", "http://photo");
    }
}

package com.example.openiaintegration.service.impl;

import com.example.openiaintegration.model.Author;
import com.example.openiaintegration.model.Book;
import com.example.openiaintegration.repo.IAuthorRepo;
import com.example.openiaintegration.repo.IBookRepo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CrudServicesTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    class AuthorServiceImplTest {

        @Mock
        private IAuthorRepo repo;

        @InjectMocks
        private AuthorServiceImpl service;

        @Test
        void findByIdReturnsStoredAuthor() throws Exception {
            Author author = new Author(1, "Gabriel", "Garcia Marquez", "Colombia", "http://photo");
            when(repo.findById(1)).thenReturn(Optional.of(author));

            assertThat(service.findById(1)).isSameAs(author);
        }

        @Test
        void findByIdReturnsEmptyAuthorWhenMissing() throws Exception {
            when(repo.findById(99)).thenReturn(Optional.empty());

            Author result = service.findById(99);

            assertThat(result).isNotNull();
            assertThat(result.getIdAuthor()).isZero();
            assertThat(result.getFirstName()).isNull();
        }

        @Test
        void saveAllAndDeleteDelegateToRepository() throws Exception {
            List<Author> authors = List.of(new Author(), new Author());
            when(repo.saveAll(authors)).thenReturn(authors);

            assertThat(service.saveAll(authors)).hasSize(2);
            service.delete(5);

            verify(repo).deleteById(5);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class BookServiceImplTest {

        @Mock
        private IBookRepo repo;

        @InjectMocks
        private BookServiceImpl service;

        @Test
        void findByIdReturnsEmptyBookWhenMissing() throws Exception {
            when(repo.findById(42)).thenReturn(Optional.empty());

            Book result = service.findById(42);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isNull();
        }

        @Test
        void updateSavesTheGivenBook() throws Exception {
            Book book = new Book();
            book.setName("Pantaleon y las visitadoras");
            when(repo.save(book)).thenReturn(book);

            assertThat(service.update(book, 3)).isSameAs(book);
            verify(repo).save(book);
        }

        @Test
        void findAllDelegatesToRepository() throws Exception {
            when(repo.findAll()).thenReturn(List.of(new Book(), new Book(), new Book()));

            assertThat(service.findAll()).hasSize(3);
        }
    }
}

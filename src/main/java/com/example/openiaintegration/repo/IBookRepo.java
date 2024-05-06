package com.example.openiaintegration.repo;

import com.example.openiaintegration.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IBookRepo extends JpaRepository<Book, Integer> {

    //FROM Book b WHERE b.name = ?
    List<Book> findByName(String name);
    Book findOneByName(String name);
}
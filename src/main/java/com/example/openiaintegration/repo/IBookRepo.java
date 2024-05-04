package com.example.openiaintegration.repo;

import com.example.openiaintegration.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IBookRepo extends JpaRepository<Book, Integer> {
}
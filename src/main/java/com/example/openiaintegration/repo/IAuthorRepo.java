package com.example.openiaintegration.repo;

import com.example.openiaintegration.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAuthorRepo extends JpaRepository<Author, Integer> {
}
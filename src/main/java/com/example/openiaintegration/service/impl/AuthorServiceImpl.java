package com.example.openiaintegration.service.impl;

import com.example.openiaintegration.model.Author;
import com.example.openiaintegration.repo.IAuthorRepo;
import com.example.openiaintegration.service.IAuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements IAuthorService {

    private final IAuthorRepo repo;
    
    @Override
    public Author save(Author author) throws Exception {
        return repo.save(author);
    }

    @Override
    public List<Author> saveAll(List<Author> t) throws Exception {
        return repo.saveAll(t);
    }

    @Override
    public Author update(Author author, Integer integer) throws Exception {
        return repo.save(author);
    }

    @Override
    public List<Author> findAll() throws Exception {
        return repo.findAll();
    }

    @Override
    public Author findById(Integer id) throws Exception {
        return repo.findById(id).orElse(new Author());
    }

    @Override
    public void delete(Integer id) throws Exception {
        repo.deleteById(id);
    }
}
package com.example.openiaintegration.service.impl;

import com.example.openiaintegration.model.Book;
import com.example.openiaintegration.repo.IBookRepo;
import com.example.openiaintegration.service.IBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements IBookService {

    private final IBookRepo repo;

    @Override
    public Book save(Book book) throws Exception {
        return repo.save(book);
    }

    @Override
    public List<Book> saveAll(List<Book> t) throws Exception {
        return repo.saveAll(t);
    }

    @Override
    public Book update(Book book, Integer integer) throws Exception {
        return repo.save(book);
    }

    @Override
    public List<Book> findAll() throws Exception {
        return repo.findAll();
    }

    @Override
    public Book findById(Integer id) throws Exception {
        return repo.findById(id).orElse(new Book());
    }

    @Override
    public void delete(Integer id) throws Exception {
        repo.deleteById(id);
    }

}

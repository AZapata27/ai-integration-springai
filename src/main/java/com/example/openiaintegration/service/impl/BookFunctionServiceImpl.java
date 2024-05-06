package com.example.openiaintegration.service.impl;

import com.example.openiaintegration.model.Book;
import com.example.openiaintegration.repo.IBookRepo;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public class BookFunctionServiceImpl implements Function<BookFunctionServiceImpl.Request, BookFunctionServiceImpl.Response> {

    private final IBookRepo repo;

    public record Request(String bookName){}
    public record Response(String review){}

    public Response apply(Request request) {
        //List<Book> books = repo.findByName(request.bookName);
        Book book = repo.findOneByName(request.bookName);
        return new BookFunctionServiceImpl.Response(book.getReview());
    }
}

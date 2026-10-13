package com.example.openiaintegration.service.impl;

import com.example.openiaintegration.model.Book;
import com.example.openiaintegration.repo.IBookRepo;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;

/**
 * Function exposed to the model as {@code BookInfo}: returns the stored review of a book by its name.
 * When the book does not exist it answers with an explicit "not found" result instead of failing,
 * so the model can tell the user instead of the request ending in a 500.
 */
@RequiredArgsConstructor
public class BookFunctionServiceImpl implements Function<BookFunctionServiceImpl.Request, BookFunctionServiceImpl.Response> {

    private final IBookRepo repo;

    public record Request(String bookName){}
    public record Response(boolean found, String review){}

    @Override
    public Response apply(Request request) {
        String bookName = request == null || request.bookName() == null ? "" : request.bookName().trim();
        if (bookName.isEmpty()) {
            return new Response(false, "No se indicó el nombre del libro.");
        }

        List<Book> books = repo.findByName(bookName);
        if (books == null || books.isEmpty()) {
            return new Response(false, "No se encontró ningún libro con el nombre '" + bookName + "'.");
        }
        return new Response(true, books.get(0).getReview());
    }
}

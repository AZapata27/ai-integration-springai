package com.example.openiaintegration.controller;

import com.example.openiaintegration.model.Book;
import com.example.openiaintegration.service.IBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookController {

    private final IBookService bookService;

    @GetMapping
    public ResponseEntity<List<Book>> findAll() throws Exception {
        return ResponseEntity.ok(bookService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> findById(@PathVariable("id") Integer id) throws Exception {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Book> save(@RequestBody Book book) throws Exception {
        Book obj = bookService.save(book);
        return ResponseEntity.created(URI.create("http://localhost:8080/books/" + obj.getIdBook())).body(obj);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> update(@RequestBody Book book, @PathVariable("id") Integer id) throws Exception {
        return ResponseEntity.ok(bookService.update(book, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Integer id) throws Exception {
        bookService.delete(id);
        return ResponseEntity.ok().build();
    }


}
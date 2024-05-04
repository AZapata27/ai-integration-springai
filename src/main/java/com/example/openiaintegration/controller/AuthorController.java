package com.example.openiaintegration.controller;

import com.example.openiaintegration.model.Author;
import com.example.openiaintegration.service.IAuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/authors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthorController {

    private final IAuthorService authorService;

    @GetMapping
    public ResponseEntity<List<Author>> findAll() throws Exception {
        return ResponseEntity.ok(authorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Author> findById(@PathVariable("id") Integer id) throws Exception {
        return ResponseEntity.ok(authorService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Author> save(@RequestBody Author author) throws Exception {
        Author obj = authorService.save(author);
        return ResponseEntity.created(URI.create("http://localhost:8080/authors/" + obj.getIdAuthor())).body(obj);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Author> update(@RequestBody Author author, @PathVariable("id") Integer id) throws Exception {
        return ResponseEntity.ok(authorService.update(author, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Integer id) throws Exception {
        authorService.delete(id);
        return ResponseEntity.ok().build();
    }

}

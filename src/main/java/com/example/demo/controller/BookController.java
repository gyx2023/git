package com.example.demo.controller;

import com.example.demo.model.Book;
import com.example.demo.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService){ this.bookService=bookService; }

    @GetMapping
    public List<Book> list(){ return bookService.findAll(); }

    @GetMapping("/{id}")
    public Book get(@PathVariable Long id){
        Book book = bookService.findById(id);
        if(book == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"用户不存在:" + id);
        }
        return book;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(@RequestBody Book book){ return bookService.create(book); }

    @PutMapping("/{id}")
    public Book update(@PathVariable Long id,@RequestBody Book book){
        Book updated = bookService.update(id,book);
        if(updated ==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"用户不存在:" + id);
        }
        return updated;
        
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id){
        if(!bookService.delete(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"用户不存在:" + id);
        }
        return "删除成功！";
    }

}

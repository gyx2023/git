package com.example.demo.service;

import com.example.demo.model.Book;
import com.example.demo.mapper.BookMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BookService {

    private final BookMapper bookMapper;

    public BookService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public List<Book> findAll() { return bookMapper.findAll(); }

    public Book findById(Long id) { return bookMapper.findById(id); }

    public Book create(Book book) {
        bookMapper.insert(book);   // 插完之后 book 的 id 会被自动填上
        return book;
    }

    public Book update(Long id, Book newBook) {
        Book old = bookMapper.findById(id);
        if (old == null) { return null; }
        old.setPrice(newBook.getPrice());
        old.setStock(newBook.getStock());
        bookMapper.update(old);    // 注意这里传的是 old
        return old;
    }

    public boolean delete(Long id) {
        return bookMapper.deleteById(id) > 0;   // 影响行数大于 0，说明真删掉了
    }
}
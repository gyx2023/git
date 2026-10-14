package com.example.demo.mapper;

import com.example.demo.model.Book;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BookMapper {

    @Select("SELECT * FROM book ORDER BY id")
    List<Book> findAll();

    @Select("SELECT * FROM book WHERE id = #{id}")
    Book findById(Long id);

    @Insert("INSERT INTO book(title, author, price, stock) VALUES(#{title}, #{author}, #{price}, #{stock})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Book book);

    @Update("UPDATE book SET title=#{title}, author=#{author}, price=#{price}, stock=#{stock} WHERE id=#{id}")
    int update(Book book);

    @Delete("DELETE FROM book WHERE id = #{id}")
    int deleteById(Long id);
}
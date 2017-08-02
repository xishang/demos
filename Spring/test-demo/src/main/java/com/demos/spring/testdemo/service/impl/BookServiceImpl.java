package com.demos.spring.testdemo.service.impl;

import com.demos.spring.testdemo.bean.Book;
import com.demos.spring.testdemo.service.BookService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Created by xishang on 2017/8/2.
 */
@Service
public class BookServiceImpl implements BookService {

    public Book getBook(String bookName) {
        Book book = new Book();
        if ("三国演义".equals(bookName)) {
            book.setName(bookName);
            book.setPrice(new BigDecimal("60.00"));
            book.setContent("天下大势，分久必合，合久必分！");
        }
        return book;
    }

}

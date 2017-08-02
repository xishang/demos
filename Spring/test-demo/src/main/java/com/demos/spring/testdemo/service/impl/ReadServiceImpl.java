package com.demos.spring.testdemo.service.impl;

import com.demos.spring.testdemo.bean.Book;
import com.demos.spring.testdemo.service.BookService;
import com.demos.spring.testdemo.service.ReadService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by xishang on 2017/8/2.
 */
@Service
public class ReadServiceImpl implements ReadService {

    @Resource
    private BookService bookService;

    public void read(String bookName) {
        Book book = bookService.getBook(bookName);
        System.out.println(book.getContent());
    }

}

package com.demos.spring.testdemo.controller;

import com.demos.spring.testdemo.bean.Book;
import com.demos.spring.testdemo.service.BookService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created by xishang on 2017/8/2.
 */
@RestController
@RequestMapping("/book")
public class BookController {

    @Resource
    private BookService bookService;

    @RequestMapping(value = "/{name}", produces = "application/json")
    public Book getBookByName(@PathVariable("name") String name) {
        return bookService.getBook(name);
    }

}

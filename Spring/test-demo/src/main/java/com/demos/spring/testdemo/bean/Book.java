package com.demos.spring.testdemo.bean;

import java.math.BigDecimal;

/**
 * Created by xishang on 2017/8/2.
 */
public class Book {

    private String name;
    private BigDecimal price;
    private String content;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "name:" + name + ", price:" + price + ", content:" + content;
    }
}

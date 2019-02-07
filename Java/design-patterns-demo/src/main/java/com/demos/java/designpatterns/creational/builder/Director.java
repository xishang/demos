package com.demos.java.designpatterns.creational.builder;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 导演类: 调用构建者进行构建
 */
public class Director {

    private Builder builder;

    public Director(Builder builder) {
        this.builder = builder;
    }

    public Product construct() {
        return builder.buildPartA()
                .buildPartB()
                .buildPartC()
                .build();
    }

}

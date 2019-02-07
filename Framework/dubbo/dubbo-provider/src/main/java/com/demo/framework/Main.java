package com.demo.framework;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/15
 */
public class Main {

    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:provider.xml");
        context.start();
        // 保持程序运行
        System.in.read();
    }

}

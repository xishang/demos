package com.demo.framework.springdemo.bean;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/25
 */
@Component
public class Orange {

    @PostConstruct
    public void init() {
        System.out.println("Orange @PostConstruct!");
    }

    public Orange() {
        System.out.println("Orange Constructor!");
    }

    @Cacheable
    public long getCacheThing() {
        return System.currentTimeMillis();
    }

}

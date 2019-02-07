package com.demo.spring.starterdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = "com.demo.spring.starterdemo.mapper")
public class StarterDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(StarterDemoApplication.class, args);
	}
}

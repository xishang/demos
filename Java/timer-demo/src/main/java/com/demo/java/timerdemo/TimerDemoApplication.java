package com.demo.java.timerdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// 开启Scheduled注解的定时任务
@EnableScheduling
public class TimerDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimerDemoApplication.class, args);
	}
}

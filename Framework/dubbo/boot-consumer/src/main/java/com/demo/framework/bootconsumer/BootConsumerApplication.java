package com.demo.framework.bootconsumer;

import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 即使是消费者也要加上`DubboComponentScan`注解, 以便扫描解析`@Reference`注解
@DubboComponentScan(basePackages = "com.demo.framework.bootconsumer.controller")
public class BootConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BootConsumerApplication.class, args);
	}
}

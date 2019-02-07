package com.demo.framework.bootprovider;

import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@DubboComponentScan(basePackages = "com.demo.framework.bootprovider.service")
public class BootProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(BootProviderApplication.class, args);
	}

}

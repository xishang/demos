package com.demo.project.casclient;

import com.demo.project.casclient.filter.AccessFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CasClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(CasClientApplication.class, args);
	}

	@Bean
	public FilterRegistrationBean MyFilterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new AccessFilter());
		registration.addUrlPatterns("/*");
		registration.setName("accessFilter");
		registration.setOrder(1);
		return registration;
	}

}

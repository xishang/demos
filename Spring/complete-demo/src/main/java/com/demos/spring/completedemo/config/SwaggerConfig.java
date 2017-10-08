package com.demos.spring.completedemo.config;

import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/10/7
 * <p>
 * Swagger-UI配置
 * 注:
 * -- 1.SwaggerConfig只能在SpringMVC上下文中定义
 * -- 2.Controller的请求参数对象属性不能存在递归引用（如属性中存在对象本身的类型）
 * -- 3.Docket实例不能延迟加载
 */
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(metaData()).select()
                // 扫描指定包中的swagger注解
                .apis(RequestHandlerSelectors.basePackage("com.demos.spring.completedemo.controller"))
                // 扫描所有有注解的api
                // .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo metaData() {
        ApiInfo apiInfo = new ApiInfo(
                "Spring MVC REST API",
                "Spring MVC REST API",
                "1.0",
                "Terms of service",
                "xishang",
                "Apache License Version 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0");
        return apiInfo;
    }

}

package com.demos.spring.completedemo.interceptor;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by xishang on 2017/8/14.
 */
public class AppInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许跨域访问，也可以在spring配置文件中配置<mvc:cors>
        if (request.getHeader("Origin") != null) {
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        // 支持"credentials", 跨域时携带cookie
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type,Origin,Accept");
        // 若为preflight请求[OPTIONS], 则直接返回
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return false;
        } else {
            return true;
        }
    }

}

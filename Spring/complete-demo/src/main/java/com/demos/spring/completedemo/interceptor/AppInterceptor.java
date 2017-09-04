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
        final String url = request.getRequestURI();

        // 暂时屏蔽
        if (!url.isEmpty()) {
            return super.preHandle(request, response, handler);
        }

        // "/data"路径的请求允许跨域访问，也可以在spring配置文件中配置<mvc:cors>
        if (url.startsWith("/data")) {
            if(request.getHeader("Origin")!=null){
                response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            }else{
                response.addHeader("Access-Control-Allow-Origin", "*");
            }
            response.addHeader("Access-Control-Allow-Credentials", "true");
        }
        return super.preHandle(request, response, handler);
    }

}

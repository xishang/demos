package com.demos.java.basedemo.proxy.dynamic.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * JDK动态代理
 */
public class JdkDynamicProxy implements InvocationHandler {

    private Object proxied;

    public JdkDynamicProxy(Object object) {
        this.proxied = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("-------------------老师好[动态代理-JDK]-------------------");
        Object obj = method.invoke(proxied, args);
        System.out.println("-------------------老师再见[动态代理-JDK]-------------------");
        return obj;
    }

}

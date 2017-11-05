package com.demos.java.basedemo.proxy.dynamic.my;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/3
 * <p>
 * 自定义动态代理增强类
 */
public class MyInvocationHandler implements InvocationHandler {

    private Object proxied;

    public MyInvocationHandler(Object object) {
        this.proxied = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("-------------------老师好[自定义动态代理]-------------------");
        Object obj = method.invoke(proxied, args);
        System.out.println("-------------------老师再见[自定义动态代理]-------------------");
        return obj;
    }

}

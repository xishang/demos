package com.demos.java.basedemo.proxy.dynamic.my;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/2
 * <p>
 * 自定义动态代理
 */
public class MyDynamicProxy {

    /**
     * 获取代理类对象
     *
     * @param clazz
     * @param handler
     * @param <T>
     * @return
     */
    public static <T> T newProxyInstance(Class<T> clazz, InvocationHandler handler) throws Exception {
        // 要代理的方法: public & !final
        Method[] proxyMethods = Arrays.stream(clazz.getMethods())
                .filter(method -> !Modifier.isFinal(method.getModifiers()))
                .collect(Collectors.toList())
                .toArray(new Method[0]);
        // 生成的代理类
        Class<?> proxyClass = MyProxyGenerator.generateAndLoadProxyClass(clazz, proxyMethods);
        // 代理类的构造方法
        Constructor c = proxyClass.getConstructor(InvocationHandler.class);
        // 创建代理类对象
        Object proxyObj = c.newInstance(handler);
        return (T) proxyObj;
    }

}

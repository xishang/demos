package com.demos.java.basedemo.proxy.dynamic.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * CGLib动态代理
 * <p>
 * 支持接口或类的动态代理(创建子类), 代理方法不能为final方法(无法重写)
 */
public class CGLibDynamicProxy {

    public static <T> T newInstance(T t) {
        // 使用自定义的Enhancer类, 用于输出CGLib生成的proxy代理类文件
        T proxied = (T) MyEnhancer.create1(t.getClass(), new MethodInterceptor() {
            @Override
            public Object intercept(Object proxy, Method method, Object[] arg2, MethodProxy arg3) throws Throwable {
                System.out.println("-------------------老师好[动态代理-CGLib]-------------------");
                Object obj = method.invoke(t, arg2);
                System.out.println("-------------------老师再见[动态代理-CGLib]-------------------");
                return obj;
            }
        });
        return proxied;
    }

    // lambda版本
    public static <T> T newInstance1(T t) {
        T proxied = (T) Enhancer.create(t.getClass(), (MethodInterceptor) (proxy, method, arg2, arg3) -> {
            System.out.println("-------------------老师好[动态代理-CGLib]-------------------");
            Object obj = method.invoke(t, arg2);
            System.out.println("-------------------老师再见[动态代理-CGLib]-------------------");
            return obj;
        });
        return proxied;
    }

}

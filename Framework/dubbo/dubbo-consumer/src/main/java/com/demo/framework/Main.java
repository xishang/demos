package com.demo.framework;

import com.alibaba.dubbo.rpc.RpcContext;
import com.demo.framework.api.HelloService;
import com.demo.framework.api.ProductService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.Future;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/15
 */
public class Main {

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:consumer.xml");
        context.start();
        // 获取远程服务代理
        HelloService helloService = context.getBean(HelloService.class);
        // 显示调用结果
        System.out.println(helloService.sayHello("Black"));
        // 获取远程服务代理: ProductService
        ProductService productService = context.getBean(ProductService.class);
        // 方法为异步执行: async="true"
        productService.addProduct("小米mix2s");
        // 执行完毕后结果会设置到RpcContext中
        Future<Long> future = RpcContext.getContext().getFuture();
        System.out.println("产品ID=" + future.get());
    }

}

package com.demo.framework.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import rx.Observable;

import java.util.concurrent.Future;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/15
 */
public class CommandHelloWorld extends HystrixCommand<String> {

    private final String name;

    public CommandHelloWorld(String name) {
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
        this.name = name;
    }

    @Override
    protected String run() {
        // a real example would do work like a network call here
        return "Hello " + name + "!";
    }

    public static void main(String[] args) throws Exception {
        // 同步方式: 实际为: queue().get()
        HystrixCommand<String> h =new CommandHelloWorld("Bob");
        System.out.println("Get: " + h.execute());
        // 异步方式: 实际为: toObservable().toBlocking().toFuture()
        Future<String> f = new CommandHelloWorld("Bob").queue();
        System.out.println("Future: " + f.get());
        // 响应式方式: Hot Observable:
        Observable<String> o = new CommandHelloWorld("Bob").observe();
        o.subscribe(str -> {
            System.out.println("Hot Observable: " + str);
        });
        // 响应式方式: Cold Observable:
        Observable<String> co = new CommandHelloWorld("Bob").toObservable();
        co.subscribe(str -> {
            System.out.println("Cold Observable: " + str);
        });
    }

}

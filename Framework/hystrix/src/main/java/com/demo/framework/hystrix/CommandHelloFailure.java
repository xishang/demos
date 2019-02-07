package com.demo.framework.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/15
 */
public class CommandHelloFailure extends HystrixCommand<String> {

    private final String name;

    public CommandHelloFailure(String name) {
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
        this.name = name;
    }

    @Override
    protected String run() {
        throw new RuntimeException("this command always fails");
    }

    /**
     * 降级方法
     * @return
     */
    @Override
    protected String getFallback() {
        return "Hello Failure " + name + "!";
    }

    public static void main(String[] args) {
        System.out.println(new CommandHelloFailure("Bob").execute());
    }

}

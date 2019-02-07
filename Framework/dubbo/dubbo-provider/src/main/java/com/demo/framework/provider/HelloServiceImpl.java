package com.demo.framework.provider;

import com.demo.framework.api.HelloService;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/15
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        String helloStr = "Hello, " + name + "!";
        System.out.println(helloStr);
        return helloStr;
    }

}

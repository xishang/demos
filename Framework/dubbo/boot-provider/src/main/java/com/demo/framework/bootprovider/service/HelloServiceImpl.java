package com.demo.framework.bootprovider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.demo.framework.api.HelloService;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/16
 */
// 使用dubbo的`Service`注解暴露服务
@Service
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "boot-provider: hello, " + name;
    }

}

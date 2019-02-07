package com.demo.framework.bootconsumer.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.demo.framework.api.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/16
 */
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    // 使用dubbo的`Reference`注解引用远程Service
    @Reference(async = true)
    private HelloService helloService;

    @GetMapping("/{name}")
    public Object sayHello(@PathVariable String name) {
        return helloService.sayHello(name);
    }

}

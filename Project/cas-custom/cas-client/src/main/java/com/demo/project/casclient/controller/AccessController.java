package com.demo.project.casclient.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/23
 */
@RestController
@RequestMapping("/access")
public class AccessController {

    @GetMapping(value = "/test")
    public String test() {
        return "Access Test!";
    }

}

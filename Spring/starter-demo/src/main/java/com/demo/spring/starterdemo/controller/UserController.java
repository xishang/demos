package com.demo.spring.starterdemo.controller;

import com.demo.spring.starterdemo.domain.User;
import com.demo.spring.starterdemo.mapper.UserMapper;
import com.demo.spring.starterdemo.property.LoginProperties;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/4
 */
@RestController
@RequestMapping(value = "/users")
public class UserController {

    @Resource
    private UserMapper userMapper;

    @Resource
    private LoginProperties properties;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public Object getUsers(User user) {
        return userMapper.select(user);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public Object addUser(User user) {
        return userMapper.insert(user);
    }

    @RequestMapping(value = "/name/{name}", method = RequestMethod.GET)
    public Object getUser(@PathVariable String name) {
        return userMapper.selectByName(name);
    }

}

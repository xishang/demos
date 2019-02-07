package com.demo.project.casserver.controller;

import com.demo.project.casserver.domain.User;
import com.demo.project.casserver.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/22
 */
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping(value = "/users")
    public List<User> getList() {
        return userService.listUsers();
    }

    @PostMapping(value = "/users")
    public User addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @GetMapping(value = "/users/{id}")
    public User getUser(@PathVariable("id") Integer id) {
        return userService.getById(id);
    }

    @PutMapping(value = "/users/{id}")
    public User updateUser(@PathVariable("id") Integer id, @RequestBody User user) {
        user.setId(id);
        return userService.updateUser(user);
    }

    @PostMapping(value = "/test2")
    public Object test2(HttpServletRequest request) {
        String some = request.getParameter("some");
        String els = request.getParameter("els");
        return "ok";
    }

    @DeleteMapping(value = "/users/{id}")
    public Boolean deleteUser(@PathVariable("id") Integer id) {
        userService.deleteUser(id);
        return true;
    }

}

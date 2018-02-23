package com.demo.project.casclient.controller;

import com.alibaba.fastjson.JSONObject;
import com.demo.project.casclient.util.HttpUtils;
import com.demo.project.casclient.util.SessionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/23
 */
@RestController
public class UserController {

    @Value("${cas.server}")
    private String casServer;

    @GetMapping(value = "login")
    public String loginPage(HttpServletRequest request) {
        request.setAttribute("service", "http://www.baidu.com");
        return "login";
    }

    @PostMapping(value = "casLogout")
    public void casLogout(@RequestParam("sessionId") String sessionId) {
        SessionUtils.invalidate(sessionId);
    }

    @GetMapping(value = "logout")
    public void logout(HttpSession session) {
        String tgc = ((JSONObject) session.getAttribute("user")).get("tgc").toString();
        HttpUtils.post(casServer + "/logout?tgc=" + tgc, "");
    }

}

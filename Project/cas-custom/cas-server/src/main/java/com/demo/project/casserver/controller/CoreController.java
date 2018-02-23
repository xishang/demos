package com.demo.project.casserver.controller;

import com.demo.project.casserver.domain.User;
import com.demo.project.casserver.dto.TokenInfo;
import com.demo.project.casserver.service.UserService;
import com.demo.project.casserver.util.CasUtils;
import com.demo.project.casserver.util.IDGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/23
 */
@Controller
public class CoreController {

    @Resource
    private UserService userService;

    @GetMapping(value = "/login")
    public String loginPage(@RequestParam("service") String service, HttpServletRequest request) {
        String TGC = Arrays.stream(request.getCookies()).filter(cookie -> "TGC".equals(cookie.getName())).map(Cookie::getValue).findAny().orElse(null);
        if (TGC == null || !CasUtils.tgcExist(TGC)) { // 未登录
            request.setAttribute("service", service);
            return "login";
        } else { // 已通过其他系统登录, 直接生成新票据
            String ticket = IDGenerator.generateUUID();
            TokenInfo tokenInfo = new TokenInfo();
            tokenInfo.setUsername(CasUtils.getTgcUser(TGC));
            tokenInfo.setTgc(TGC);
            CasUtils.addTicket(ticket, tokenInfo);
            return "redirect:" + service + "?ticket=" + ticket;
        }
    }

    @PostMapping(value = "/login")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          @RequestParam("service") String service,
                          HttpServletResponse response) {
        User user = userService.getByUsername(username);
        // 登录失败, 直接返回资源页
        if (user == null || !user.getPassword().equals(password)) {
            return "redirect:" + service;
        }
        // 登录成功, 终端用户设置Cookie TGC(ticket-granting cookie)-授权的票据证明
        String TGC = "TGC-" + IDGenerator.generateUUID();
        response.addCookie(new Cookie("TGC", TGC));
        CasUtils.addTgc(TGC);
        CasUtils.setTgcUser(TGC, username);
        String ticket = IDGenerator.generateUUID();
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setUsername(username);
        tokenInfo.setTgc(TGC);
        CasUtils.addTicket(ticket, tokenInfo);
        return "redirect:" + service + "?ticket=" + ticket;
    }

    /**
     * 验证票据
     *
     * @param ticket    票据
     * @param logoutUrl 应用登出url
     * @return
     */
    @PostMapping(value = "/validate")
    @ResponseBody
    public TokenInfo validate(@RequestParam("ticket") String ticket,
                              @RequestParam("logoutUrl") String logoutUrl) {
        TokenInfo tokenInfo = CasUtils.getTicket(ticket);
        if (tokenInfo == null) {
            return null;
        }
        // 安全起见, 票据只能认证一次
        CasUtils.removeTicket(ticket);
        // 验证通过, 添加登录url到TGC
        CasUtils.addTgcService(tokenInfo.getTgc(), logoutUrl);
        // 返回username
        return tokenInfo;
    }

    @PostMapping(value = "logout")
    @ResponseBody
    public void logout(@RequestParam("tgc") String tgc) {
        CasUtils.tgcLogout(tgc);
    }

}

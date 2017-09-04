package com.demos.spring.completedemo.controller;

import com.demos.spring.completedemo.bean.ResponseResult;
import com.demos.spring.completedemo.bean.SimplePageInfo;
import com.demos.spring.completedemo.bean.UserDO;
import com.demos.spring.completedemo.exception.BusinessException;
import com.demos.spring.completedemo.exception.ErrorEnum;
import com.demos.spring.completedemo.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private UserService userService;

    /**
     * 用户登陆接口
     *
     * @param username   用户名或手机号
     * @param password   登陆密码
     * @param rememberMe 是否开启记住密码功能
     * @param session    当前请求的session
     * @return
     */
    @RequestMapping("/login")
    public ResponseResult<String> login(@RequestParam("username") String username, @RequestParam("password") String password,
                                        @RequestParam(value = "rememberMe", required = false, defaultValue = "false") Boolean rememberMe,
                                        HttpSession session) {
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        token.setRememberMe(rememberMe);
        try {
            // 此处为主动调用 Subject.login() 方法执行认证，也可通过配置'loginUrl'和'authc'过滤器进行认证
            // 此种方法可以通过继承 FormAuthenticationFilter 进行自定义
            SecurityUtils.getSubject().login(token);
            return new ResponseResult(ErrorEnum.SUCCESS, session.getId());
        } catch (UnknownAccountException uae) {
            return new ResponseResult(ErrorEnum.USERNAME_NOT_EXIST);
        } catch (LockedAccountException lae) {
            return new ResponseResult(ErrorEnum.ACCOUNT_LOCKED);
        } catch (IncorrectCredentialsException ice) {
            return new ResponseResult(ErrorEnum.PASSWORD_ERROR);
        } catch (Exception e) {
            return new ResponseResult(ErrorEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 用户注册接口
     *
     * @param user 封装了用户信息
     * @return
     */
    @RequestMapping("/register")
    public ResponseResult register(UserDO user) {
        try {
            // 原始密码，保存用户时对密码进行了加密处理
            String originPassword = user.getPassword();
            userService.saveUser(user);
            // 注册成功进行用户认证（登陆）
            UsernamePasswordToken token = new UsernamePasswordToken(user.getUsername(), originPassword);
            SecurityUtils.getSubject().login(token);
            return new ResponseResult(ErrorEnum.SUCCESS);
        } catch (BusinessException e) {
            return new ResponseResult(e.getErrorEnum());
        }
    }

    @RequestMapping("/list")
    public ResponseResult<SimplePageInfo<UserDO>> listUsers(int page, int size) {
        logger.info("------->getUserList, page={}, size={}", page, size);
        List<UserDO> userList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserDO user = new UserDO();
            user.setId((long) i);
            user.setUsername("用户名:" + i);
            user.setRealName("姓名:" + i);
            user.setStatus(0);
            userList.add(user);
        }
        SimplePageInfo<UserDO> userPage = new SimplePageInfo<>(userList);
        userPage.setPageNum(page);
        userPage.setPageSize(size);
        userPage.setPages(5);
        userPage.setTotal(45);
        return new ResponseResult<>(ErrorEnum.SUCCESS, userPage);
    }

}

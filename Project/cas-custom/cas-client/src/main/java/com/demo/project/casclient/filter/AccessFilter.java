package com.demo.project.casclient.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.demo.project.casclient.util.HttpUtils;
import com.demo.project.casclient.util.SessionUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/23
 */
public class AccessFilter implements Filter {

    String accessInfo = "access";
    String casServer = "http://localhost:8080";

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String url = httpRequest.getRequestURI();
        // 访问限制资源且未登录
        HttpSession session = httpRequest.getSession();
        if (url.contains(accessInfo) && session.getAttribute("user") == null) {
            String ticket = httpRequest.getParameter("ticket");
            if (ticket == null) { // 票据未null, 重定向到cas-server登录页面
                ((HttpServletResponse) response).sendRedirect(casServer + "/login?service=" + httpRequest.getRequestURL());
            } else {
                // 验证票据是否有效
                String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + httpRequest.getContextPath();
                String logoutUrl = baseUrl + "/casLogout?token=" + session.getId();
                String result = HttpUtils.post(casServer + "/validate?ticket=" + ticket + "&logoutUrl=" + logoutUrl, "");
                JSONObject json = JSON.parseObject(result);
                if (result != null) { // 验证成功
                    session.setAttribute("user", json);
                    SessionUtils.setSession(session);
                    chain.doFilter(request, response);
                }
            }
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}

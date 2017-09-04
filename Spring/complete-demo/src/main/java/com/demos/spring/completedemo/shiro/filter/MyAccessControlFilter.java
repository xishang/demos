package com.demos.spring.completedemo.shiro.filter;

import org.apache.shiro.web.filter.AccessControlFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * 自定义权限验证Filter
 * 实际上系统自带的Filter基本可以满足需求，包括：
 * <p>
 * 1.认证过滤器：anon,authc,authcBasic,user
 * === authc为认证过滤器，配置该过滤器的请求需要认证成功才可以访问
 * === 配置了authc的URL，若为'shiroFilter'中配置的loginUrl，则会获取该请求的username、password等参数，创建token并执行认证
 * === 也可在程序中主动调用 Subject.login(AuthenticationToken) 方法进行用户认证
 *
 * === user[开启rememberMe时应该使用user]表示被Shiro记住过登录状态的用户就可以正常发起请求
 *
 * 2.授权过滤器：roles,perms,port,rest,ssl
 * 3.登出过滤器：logout
 */
public class MyAccessControlFilter extends AccessControlFilter {

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        /*String[] roles = (String[]) mappedValue;
        if (roles == null || roles.length == 0) {
            return true;//如果没有设置角色参数，默认成功
        }
        for (String role : roles) {
            if (getSubject(request, response).hasRole(role)) {
                return true;
            }
        }
        return false;*/
        return true; // 空Filter，仅作为示例
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        return false;
    }

}

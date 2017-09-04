package com.demos.spring.completedemo.shiro.realm;

import com.demos.spring.completedemo.bean.PermissionDO;
import com.demos.spring.completedemo.bean.RoleDO;
import com.demos.spring.completedemo.bean.UserDO;
import com.demos.spring.completedemo.service.PermissionService;
import com.demos.spring.completedemo.service.RoleService;
import com.demos.spring.completedemo.service.UserService;
import com.demos.spring.completedemo.util.EncodeUtils;
import com.demos.spring.completedemo.util.PasswordHelper;
import com.demos.spring.completedemo.util.SystemConstant;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

public class MyAuthorizingRealm extends AuthorizingRealm {

    @Resource
    private UserService userService;

    @Resource
    private RoleService roleService;

    @Resource
    private PermissionService permissionService;

    /**
     * 授权：获取用户拥有的权限
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        // PrincipalCollection为认证方法doGetAuthenticationInfo()返回值中传入的第一个参数
        Object user = principalCollection.getPrimaryPrincipal();
        UserDO userDO = (UserDO) user;
        // 授权信息
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 添加角色
        List<RoleDO> roleList = roleService.listRoleByUserId(userDO.getId());
        for (RoleDO roleDO : roleList) {
            info.addRole(roleDO.getRoleKey());
        }
        // 添加权限
        List<PermissionDO> permissionList = permissionService.listPermissionByUserId(userDO.getId());
        for (PermissionDO permissionDO : permissionList) {
            info.addStringPermission(permissionDO.getPermissionKey());
        }
        return info;
    }

    /**
     * 认证：用户登录信息确认
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
        String username = usernamePasswordToken.getUsername();
        UserDO userDO = userService.getUserByUsername(username);
        if(userDO == null) {
            userDO = userService.getUserByPhone(username);
            if (userDO == null) {
                throw new UnknownAccountException(); // 没找到帐号
            }
        }
        if(!SystemConstant.USER_STATUS_NORMAL.equals(userDO.getStatus())) {
            throw new LockedAccountException(); // 帐号锁定
        }
        // 设置用户信息与加密信息，shiro框架会调用验证
        byte[] salt = EncodeUtils.decodeHex(userDO.getPassword().substring(0,16));
        // 把UserDO作为认证信息principal，需要实现Serializable接口，否则会导致序列化异常
        return new SimpleAuthenticationInfo(
                userDO, userDO.getPassword().substring(16),
                ByteSource.Util.bytes(salt), "myAuthorizingRealm");
    }

    /**
     * 初始化方法，设定密码校验的Hash算法与迭代次数
     */
    @PostConstruct
    public void initCredentialsMatcher() {
        HashedCredentialsMatcher matcher = new RetryLimitHashedCredentialsMatcher(PasswordHelper.HASH_ALGORITHM);
        matcher.setHashIterations(PasswordHelper.HASH_INTERATIONS);
        setCredentialsMatcher(matcher);
    }

    class RetryLimitHashedCredentialsMatcher extends HashedCredentialsMatcher {

        public RetryLimitHashedCredentialsMatcher(String hashAlgorithmName) {
            super(hashAlgorithmName);
        }

        // 验证登陆信息时调用
        @Override
        public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
            /*com.rd.ifaes.common.security.shiro.UsernamePasswordToken uptoken = (com.rd.ifaes.common.security.shiro.UsernamePasswordToken) token;
            String userName = uptoken.getUsername();
            //retry count + 1
            @SuppressWarnings("unchecked")
            Map<String, AtomicInteger> loginFailMap =  (Map<String, AtomicInteger>) CacheUtils.get("loginFailMap", Map.class);
            if (loginFailMap==null){
                loginFailMap = Maps.newHashMap();
            }
            AtomicInteger loginFailNum = loginFailMap.get(userName);
            if(loginFailNum==null){
                loginFailNum = new AtomicInteger(0);
            }
//				//if retry count > 5 throw
//		        if(loginFailNum.incrementAndGet() >= 3) {
//		            throw new ExcessiveAttemptsException("msg:您已连续3次输入密码错误，账户已被锁定");
//		        }
            loginFailMap.put(userName, loginFailNum);
            CacheUtils.set("loginFailMap", loginFailMap, ExpireTime.FIVE_MIN);
            boolean matches = super.doCredentialsMatch(token, info);
            if(matches) {
                //clear retry count
                CacheUtils.del(userName);
            }
            return matches;*/

            return super.doCredentialsMatch(token, info);
        }
    }

}

package com.shiroTest.config.shiro;



import com.shiroTest.function.role.service.impl.RoleServiceImpl;
import com.shiroTest.function.user.model.User;
import com.shiroTest.utils.JwtUtil;
import com.shiroTest.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyRealm extends AuthorizingRealm {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    RoleServiceImpl roleService;



    public static final String USER_KEY_PREFIX = "token_";

    /**
     * 限定这个realm只能处理JwtToken
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 获取到用户名，查询用户权限
        SimpleAuthorizationInfo simpleAuthorizationInfo=new SimpleAuthorizationInfo();
        User user = (User) principals.getPrimaryPrincipal();
        if (StringUtils.isNotEmpty(user.getRoleId()) ){
            List<String> permissions = roleService.getRolePermissions(user.getRoleId());
            simpleAuthorizationInfo.addStringPermissions(permissions);
        }

        return simpleAuthorizationInfo;
    }


    /**
     * 认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken){
        // 获取token信息
        String token = (String) authenticationToken.getCredentials();
        // 校验token：未校验通过或者已过期
        String tokenKey = redisUtil.buildUserTokenKey(token);
        if (!jwtUtil.verifyToken(token) || jwtUtil.isExpire(token)) {
            redisUtil.delete(tokenKey);
            throw new AuthenticationException("token已失效，请重新登录");
        }
        // 用户信息
        User user = (User) redisUtil.get(tokenKey);
        if (null == user) {
            throw new UnknownAccountException("用户不存在");
        }
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(user, token, this.getName());
        return simpleAuthenticationInfo;
    }

    @Override
    protected boolean isPermitted(Permission permission, AuthorizationInfo info) {
        boolean permitted = super.isPermitted(permission, info);
        if (!permitted){
            throw new AuthenticationException("no authorization:"+permission.toString());
        }
        return true;
    }
}

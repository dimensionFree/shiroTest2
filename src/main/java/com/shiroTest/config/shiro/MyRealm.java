package com.shiroTest.config.shiro;



import com.shiroTest.function.user.model.User;
import com.shiroTest.utils.JwtUtil;
import com.shiroTest.utils.RedisUtil;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyRealm extends AuthorizingRealm {

    public static final String USER_KEY_PREFIX = "token_";

    /**
     * 限定这个realm只能处理JwtToken
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 授权(授权部分这里就省略了)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 获取到用户名，查询用户权限
        return null;
    }

    /**
     * 认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken){
        // 获取token信息
        String token = (String) authenticationToken.getCredentials();
        // 校验token：未校验通过或者已过期
        if (!JwtUtil.verifyToken(token) || JwtUtil.isExpire(token)) {
            throw new AuthenticationException("token已失效，请重新登录");
        }
        // 用户信息
        User user = (User) RedisUtil.get(USER_KEY_PREFIX + token);
        if (null == user) {
            throw new UnknownAccountException("用户不存在");
        }
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(user, token, this.getName());
        return simpleAuthenticationInfo;
    }
}

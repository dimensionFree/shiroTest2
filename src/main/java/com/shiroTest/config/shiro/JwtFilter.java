package com.shiroTest.config.shiro;

import cn.hutool.json.JSONUtil;
import com.shiroTest.common.Result;
import com.shiroTest.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Set;

@Component
@Slf4j
public class JwtFilter extends BasicHttpAuthenticationFilter {

    @Autowired
    RedisUtil redisUtil;

    private Set<Autority> authoritySet;

    private boolean adminOnly=false;

    public JwtFilter(Set<Autority> authoritySet, boolean adminOnly) {
        this.authoritySet = authoritySet;
        this.adminOnly = adminOnly;
    }

    public JwtFilter(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    public JwtFilter(Set<Autority> authoritySet) {
        this.authoritySet = authoritySet;
    }

    public JwtFilter() {
    }

    private String errorMsg;

    // 过滤器拦截请求的入口方法
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 判断请求头是否带上“Token”
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String authorization = httpServletRequest.getHeader("Authorization");
        String token = StringUtils.isEmpty(authorization)?"":authorization.split(" ")[1];

        // 游客访问电商平台首页可以不用携带 token
//        if (StringUtils.isEmpty(token)) {
//            return true;
//        }
        try {
            // 交给 myRealm
            checkAdmin();
            checkAuthority();
            SecurityUtils.getSubject().login(new JwtToken(token));
            return true;
        } catch (Exception e) {
            errorMsg = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    private void checkAuthority() {

    }

    private void checkAdmin() {

    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setStatus(400);
        httpServletResponse.setContentType("application/json;charset=utf-8");
        PrintWriter out = httpServletResponse.getWriter();
        out.println(JSONUtil.toJsonStr(Result.fail(errorMsg)));
        out.flush();
        out.close();
        return false;
    }

    /**
     * 对跨域访问提供支持
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域发送一个option请求
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }

}

package com.shiroTest.config.shiro;

import cn.hutool.json.JSONUtil;
import com.shiroTest.common.Result;
import com.shiroTest.utils.JwtUtil;
import com.shiroTest.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Component
@Slf4j
public class JwtFilter extends BasicHttpAuthenticationFilter {

    @Autowired
    RedisUtil redisUtil;


    @Autowired
    JwtUtil jwtUtil;



    public JwtFilter() {
    }

    private String errorMsg;

    // 过滤器拦截请求的入口方法
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 判断请求头是否带上“Token”
//        String token = jwtUtil.getTokenFromRequest((HttpServletRequest) request);
        //todo:extract getToken to method
        HttpServletRequest httpServletRequest =(HttpServletRequest) request;
        String authorization = httpServletRequest.getHeader("Authorization");
        var token= StringUtils.isEmpty(authorization)?"":authorization.split(" ")[1];
        try {

            // 交给 myRealm
            SecurityUtils.getSubject().login(new JwtToken(token));


            boolean permitted = getPermittedByPermStr((String[]) mappedValue);
            return permitted;
        } catch (Exception e) {
            errorMsg = "权限不足："+e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    private boolean getPermittedByPermStr(String[] mappedValue) {
        String[] rawStr = mappedValue;
        var seperatedPerms = getSeperatedPerms(rawStr);
        List<String> logic = seperatedPerms.getLeft();
        List<String> perms = seperatedPerms.getRight();
        return getPermsResultWithLogic(perms,logic);
    }

    private boolean getPermsResultWithLogic(List<String> perms, List<String> logics) {
        boolean[] permitted = SecurityUtils.getSubject().isPermitted(perms.toArray(new String[perms.size()]));
        boolean result=permitted[0];
        for (int i = 0; i < logics.size(); i++) {
            String logic = logics.get(i);
            if (ShiroConfig.PERMS_AND.equals(logic)){
                result=result&&permitted[i+1];
            }else if (ShiroConfig.PERMS_OR.equals(logic)){
                result=result||permitted[i+1];
            }
        }
        return result;
    }

    private Pair<List<String>,List<String>> getSeperatedPerms(String[] rawStr) {
        // 初始化两个空列表来分别存储逻辑关键字和权限字符串
        List<String> logicList = new ArrayList<>();
        List<String> permissionsList = new ArrayList<>();

        for (String item : rawStr) {
            if (ShiroConfig.LOGIC_STR_SET.contains(item)) {
                logicList.add(item);
            } else {
                permissionsList.add(item);
            }
        }
        return Pair.of(logicList,permissionsList);
    }

    @Override
    protected void cleanup(ServletRequest request, ServletResponse response, Exception existing) throws ServletException, IOException {
        if (Objects.nonNull(existing)){
            Result fail = Result.fail(existing);
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(fail.getStatusCode().value());
            httpServletResponse.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();
            out.println(JSONUtil.toJsonStr(fail));
            out.flush();
            out.close();
        }

        super.cleanup(request, response, existing);
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

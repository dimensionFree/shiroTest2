package com.shiroTest.config.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


@Configuration
@ComponentScan(value = "com.shiroTest.config.shiro")
public class ShiroConfig {

    public static final String PERMS_OR = "or";
    public static final String PERMS_AND = "and";

    public static final Set<String> LOGIC_STR_SET= Set.of(PERMS_OR,PERMS_AND);

    // 1.shiroFilter：负责拦截所有请求
    @Bean
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager defaultWebSecurityManager) {

        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 给filter设置安全管理器
        shiroFilterFactoryBean.setSecurityManager(defaultWebSecurityManager);
//        // 默认认证界面路径---当认证不通过时跳转
//        shiroFilterFactoryBean.setLoginUrl("/login.jsp");

        // 添加自己的过滤器并且取名为jwt
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("jwt", new JwtFilter());
        shiroFilterFactoryBean.setFilters(filterMap);

        // 配置系统受限资源 path match at PathMatchingFilterChainResolver.getChain
        //need linkedhashmap to keep it sorted
        Map<String, String> map = new LinkedHashMap<>();
//        map.put("/index.jsp", "authc");
//        map.put("/login.jsp","anon");
        //user
        map.put("/api/user/login","anon");
        map.put("/api/user/register","anon");
        map.put("/api/user/send-verification-code","anon");
        map.put("/api/user/create","jwt[USER_CREATE]");
        map.put("/api/user/find/**","jwt[USER_READ]");
        map.put("/api/user/findAll/**","jwt[USER_READ]");
        map.put("/api/user/update/**","jwt[USER_EDIT,"+ PERMS_OR +",USER_EDIT_SELF]");
        map.put("/api/user/delete/**","jwt[USER_EDIT,"+ PERMS_OR +",USER_EDIT_SELF]");
        //role
        map.put("/api/role/findAll/**","jwt[USER_EDIT,"+ PERMS_OR +",USER_EDIT_SELF]");

        //article
        map.put("/api/article/read/latest","anon");
        map.put("/api/article/read/detail/**","jwt[ARTICLE_READ]");
        map.put("/api/article/manage/public/**","jwt[ARTICLE_EDIT,"+ PERMS_OR +",ARTICLE_EDIT_SELF]");
        map.put("/api/article/manage/**","jwt[ARTICLE_READ]");
        map.put("/api/article/find/**","anon");
        map.put("/api/article/findAll/**","anon");
        map.put("/api/assistant/context","anon");
        map.put("/api/article/create","jwt[ARTICLE_CREATE]");
        map.put("/api/article/update/**","jwt[ARTICLE_EDIT,"+ PERMS_OR +",ARTICLE_EDIT_SELF]");
//        map.put("/api/article/delete/**","jwt[ARTICLE_EDIT,"+ PERMS_OR +",ARTICLE_EDIT_SELF]");


        map.put("/**", "jwt");   // 所有请求通过我们自己的过滤器

        shiroFilterFactoryBean.setFilterChainDefinitionMap(map);

        return shiroFilterFactoryBean;
    }

    //2.创建安全管理器
    @Bean
    public DefaultWebSecurityManager getDefaultWebSecurityManager(MyRealm realm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 给安全管理器设置realm
        securityManager.setRealm(realm);
        // 关闭shiro的session（无状态的方式使用shiro）
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);
        return securityManager;
    }
}

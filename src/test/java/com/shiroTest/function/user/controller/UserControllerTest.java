package com.shiroTest.function.user.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.shiroTest.BackendApplication;
import com.shiroTest.common.ResultData;
import com.shiroTest.function.quickMenu.MenuItem;
import com.shiroTest.function.role.model.Authority;
import com.shiroTest.function.role.service.impl.RoleServiceImpl;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.model.UserLoginInfo;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Set;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@EnableWebMvc
//@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BackendApplication.class})
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
//@WebMvcTest(UserController.class)
//自动回滚数据库
@Transactional
public class UserControllerTest extends TestCase {


    public static final String USER_ID_ADMIN = "user_id_admin";
    public static final String USER_ID_MEMBER = "user_id_member";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    public UserServiceImpl service;

    @Test
//    @Rollback
    public void testRegister() throws Exception {
        String inputUsername = "username";
        String inputPwd = "password";
        User user = new User(inputUsername, inputPwd);
        String s = JSONUtil.toJsonStr(user);

        // 构建一个POST请求
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("http://localhost/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(s);


        // 发送请求并验证结果
        MvcResult result = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(result);

        // 验证内容类型
        String contentType = result.getResponse().getContentType();
        assertTrue(contentType.startsWith("application/json"));


        // 获取响应内容
        String jsonResponse = result.getResponse().getContentAsString();

        // 将响应内容转换为User对象
        var resultData = JSONUtil.toBean(jsonResponse, ResultData.class);

        System.out.println(resultData);
        UserLoginInfo dataContent = JSONUtil.toBean((JSONObject) resultData.getDataContent(),UserLoginInfo.class);
        // 进行断言验证
        assertNotNull(dataContent);
        assertEquals(dataContent.getUser4Display().getUsername(),inputUsername);
        assertEquals(dataContent.getUser4Display().getRoleId(), RoleServiceImpl.ROLE_ID_MEMBER);
        assertNotNull(dataContent.getUser4Display().getRole());
        assertEquals(dataContent.getUser4Display().getRole().getAuthorities(), Set.of(Authority.ARTICLE_EDIT,Authority.ARTICLE_READ));

    }

    @Test
    public void testLogin() {
    }
}
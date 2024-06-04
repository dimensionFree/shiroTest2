package com.shiroTest.function.user.controller;

import cn.hutool.json.JSONUtil;
import com.shiroTest.BackendApplication;
import com.shiroTest.function.quickMenu.MenuItem;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@EnableWebMvc
//@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BackendApplication.class})
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
//@WebMvcTest(UserController.class)
public class UserControllerTest extends TestCase {


    public static final String USER_ID_ADMIN = "user_id_admin";
    public static final String USER_ID_MEMBER = "user_id_member";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    public UserServiceImpl service;

    @Test
    public void testRegister() throws Exception {
        User user = new User("username", "password");
        String s = JSONUtil.toJsonStr(user);

        // 构建一个POST请求
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("http://localhost/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(s);

        try {
            // 发送请求并验证结果
            mockMvc.perform(requestBuilder)
                    .andExpect(status().isOk())
                    .andExpect(content().string(notNullValue()));


        } finally {

        }
    }

    @Test
    public void testLogin() {
    }
}
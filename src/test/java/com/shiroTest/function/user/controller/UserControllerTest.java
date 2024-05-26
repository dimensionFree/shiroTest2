package com.shiroTest.function.user.controller;

import cn.hutool.json.JSONUtil;
import com.shiroTest.BackendApplication;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApplication.class)
public class UserControllerTest extends TestCase {

    @Autowired
    private MockMvc mockMvc;

    // 在测试类中添加@Before注解的方法
    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController()).build();
    }


    @Test
    public void testRegister() throws Exception {
        String s = JSONUtil.toJsonStr(new User("username", "password"));

        // 构建一个POST请求
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("http://localhost/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(s);

        try {
            // 发送请求并验证结果
            //todo:service is null
            mockMvc.perform(requestBuilder)
                    .andExpect(status().isOk())
                    .andExpect(content().string(notNullValue()));

        } finally {

        }
    }

    public void testLogin() {
    }
}
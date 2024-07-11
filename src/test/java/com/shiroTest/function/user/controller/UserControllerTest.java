package com.shiroTest.function.user.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.common.ResultData;
import com.shiroTest.function.base.BaseControllerTest;
import com.shiroTest.function.role.model.Authority;
import com.shiroTest.function.role.service.impl.RoleServiceImpl;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.model.User4Display;
import com.shiroTest.function.user.model.UserLoginInfo;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import com.shiroTest.utils.JsonUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;import static org.assertj.core.api.Assertions.assertThat;


public class UserControllerTest extends BaseControllerTest {


    @Override
    protected ServiceImpl getService() {
        return service;
    }

    @Override
    protected String getApiPrefix() {
        return "/user";
    }



    @Autowired
    public UserServiceImpl service;

    @Test
    public void crud_should_work() throws Exception {
        String inputUsername = "username";
        String inputPwd = "password";
        User user = new User(inputUsername, inputPwd);
        member_test_CRUD(user);
    }


    @Test
    public void testRegister() throws Exception {
        String inputUsername = "username";
        String inputPwd = "password";
        User user = new User(inputUsername, inputPwd);
        String s = JsonUtil.toJson(user);

        // 构建一个POST请求
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post(HTTP_LOCALHOST + "/user/register")
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
        var resultData = JsonUtil.fromJson(jsonResponse, ResultData.class);

        System.out.println(resultData);

        UserLoginInfo dataContent = JsonUtil.fromJson(JsonUtil.toJson(resultData.getDataContent()),UserLoginInfo.class);
        // 进行断言验证
        assertNotNull(dataContent);
        User4Display user4Display = dataContent.getUser4Display();
        assertEquals(user4Display.getUsername(),inputUsername);
        assertEquals(user4Display.getRoleId(), RoleServiceImpl.ROLE_ID_MEMBER);
        assertNotNull(user4Display.getRole());
        assertThat(user4Display.getRole().getAuthorities()).contains(Authority.ARTICLE_EDIT,Authority.ARTICLE_READ,Authority.USER_READ_SELF,Authority.USER_EDIT_SELF);
        assertThat(user4Display.getCreateBy()).isEqualTo(user4Display.getId());
        LocalDateTime createDate = user4Display.getCreateDate();
        LocalDateTime now = LocalDateTime.now();
        assertThat(Math.abs(createDate.getYear()-now.getYear())).isLessThanOrEqualTo(1);
        assertThat(Math.abs(createDate.getMonthValue() - now.getMonthValue())).isLessThanOrEqualTo(1);
        assertThat(Math.abs(createDate.getDayOfMonth() - now.getDayOfMonth())).isLessThanOrEqualTo(1);
        assertThat(Math.abs(createDate.getHour() - now.getHour())).isLessThanOrEqualTo(1);
        assertThat(Math.abs(createDate.getMinute() - now.getMinute())).isLessThanOrEqualTo(1);
    }

    @Test
    public void testLogin() {
    }


}
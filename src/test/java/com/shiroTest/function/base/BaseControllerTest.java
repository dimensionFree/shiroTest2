package com.shiroTest.function.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.BackendApplication;
import com.shiroTest.common.MyException;
import com.shiroTest.common.Result;
import com.shiroTest.common.ResultData;
import com.shiroTest.function.role.model.Authority;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.model.User4Display;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import com.shiroTest.utils.JsonUtil;
import com.shiroTest.utils.RedisUtil;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@EnableWebMvc
@WebAppConfiguration
//@RunWith(SpringJUnit4ClassRunner.class)(todo:what the diff with SpringRunner.class?)
//@WebMvcTest(UserController.class)
public abstract class BaseControllerTest extends BaseTest {


    public static final String USER_ID_ADMIN = "user_id_admin";
    public static final String USER_ID_MEMBER = "user_id_member";

    public UserServiceImpl getUserService() {
        return userService;
    }

    User admin;
    User member;
    String adminToken;
    String memberToken;

    @Autowired
    protected RedisUtil redisUtil;

    @Before
    public void init() throws MyException {
        var adminLogin = userService.loginUser("admin", "adminPwd");
        var memberLogin = userService.loginUser("member", "memberPwd");
        adminToken=adminLogin.getToken();
        memberToken=memberLogin.getToken();

        User4Display admin4Display = (User4Display)redisUtil.get(adminToken);
//        assertThat(admin4Display).isNotNull();
        User4Display member4Display = (User4Display)redisUtil.get(memberToken);
//        assertThat(member4Display).isNotNull();
    }

    @Autowired
    private UserServiceImpl userService;

    public static final String HTTP_LOCALHOST = "http://localhost";

    protected abstract ServiceImpl getService();

    protected abstract String getApiPrefix();


    protected  <T extends BaseEntity> void member_test_CRUD(T data) throws Exception {
        //create
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post(HTTP_LOCALHOST +getApiPrefix()+"/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(data));

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
        assertThat((Boolean) resultData.getDataContent()).isTrue();



        //read
        requestBuilder = MockMvcRequestBuilders
                .get(HTTP_LOCALHOST +getApiPrefix()+"/find/"+data.)
                .header("Authorization","Bearer "+adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(data));

        // 发送请求并验证结果
        result = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(result);

        // 验证内容类型
        contentType = result.getResponse().getContentType();
        assertTrue(contentType.startsWith("application/json"));

        // 获取响应内容
        jsonResponse = result.getResponse().getContentAsString();

        // 将响应内容转换为User对象
        resultData = JsonUtil.fromJson(jsonResponse, ResultData.class);
        assertThat(resultData).isNotNull();



    }
}

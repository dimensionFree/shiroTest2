package com.shiroTest.function.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.BackendApplication;
import com.shiroTest.common.MyException;
import com.shiroTest.common.ResultData;
import com.shiroTest.config.shiro.JwtFilter;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.model.User4Display;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import com.shiroTest.utils.JsonUtil;
import com.shiroTest.utils.RedisUtil;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.restassured.RestAssured.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@AutoConfigureMockMvc(addFilters = false)
@EnableWebMvc
//@WebAppConfiguration(removed cause:should only be used with @SpringBootTest when @SpringBootTest is configured with a mock web environment. Please remove @WebAppConfiguration or reconfigure @SpringBootTest.)
//@RunWith(SpringJUnit4ClassRunner.class)(todo:what the diff with SpringRunner.class?)
@SpringBootTest(classes = BackendApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@WebMvcTest(UserController.class)
public abstract class BaseControllerTest extends BaseTest {


    @Autowired
    protected PlatformTransactionManager transactionManager;

    @LocalServerPort
    private int port;


    public static final String USER_ID_ADMIN = "user_id_admin";
    public static final String USER_ID_MEMBER = "user_id_member";

    protected abstract ServiceImpl getService();

    public UserServiceImpl getUserService() {
        return userService;
    }

    protected User admin;
    protected User member;
    protected String adminToken;
    protected String memberToken;

    @Autowired
    protected RedisUtil redisUtil;

    final String HOST=getHost();

    @BeforeAll
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

    public final String HTTP_LOCALHOST = "http://localhost";


    protected abstract String getApiPrefix();

    protected String getHost(){
        return HTTP_LOCALHOST+":"+port;
    }



    protected  <T extends BaseEntity> void member_test_CRUD(T data) throws Exception {
        List<Runnable> tasks = new ArrayList<>();
        Map<String, Object> dataMap = JsonUtil.toMap(data);
        String id = dataMap.get("id").toString();

//        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
//

        try {
            // 在try块中定义任务
            tasks.add(() -> {
                        getLog().info("Deleting data...");
                        // 删除数据的实际逻辑
                        getService().removeById(id);
                    }
            );

            ResultData as = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+adminToken)
                    .body(data)
                    .when()
                    .post(getHost() + getApiPrefix() + "/create")
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);


            assertThat(as).isNotNull();
        } finally {
            processDeleteTask(tasks);
        }
//        //create
//        RequestBuilder requestBuilder = MockMvcRequestBuilders
//                .post(HTTP_LOCALHOST +getApiPrefix()+"/create")
//                .header("Authorization","Bearer "+adminToken)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(JsonUtil.toJson(data));
//
//        // 发送请求并验证结果
//        MvcResult result = mockMvc.perform(requestBuilder)
//                .andExpect(status().isOk())
//                .andReturn();
//
//        System.out.println(result);
//
//        // 验证内容类型
//        String contentType = result.getResponse().getContentType();
//        assertTrue(contentType.startsWith("application/json"));
//
//        // 获取响应内容
//        String jsonResponse = result.getResponse().getContentAsString();
//
//        // 将响应内容转换为User对象
//        var resultData = JsonUtil.fromJson(jsonResponse, ResultData.class);
//        assertThat((Boolean) resultData.getDataContent()).isTrue();



    }


}

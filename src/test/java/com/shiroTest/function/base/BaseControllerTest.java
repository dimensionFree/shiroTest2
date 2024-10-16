package com.shiroTest.function.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageInfo;
import com.shiroTest.BackendApplication;
import com.shiroTest.common.MyException;
import com.shiroTest.common.ResultData;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.model.User4Display;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import com.shiroTest.utils.DeleteDataHelper;
import com.shiroTest.utils.JsonUtil;
import com.shiroTest.utils.RedisUtil;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

//@AutoConfigureMockMvc(addFilters = false)
@EnableWebMvc
//@WebAppConfiguration(removed cause:should only be used with @SpringBootTest when @SpringBootTest is configured with a mock web environment. Please remove @WebAppConfiguration or reconfigure @SpringBootTest.)
//@RunWith(SpringJUnit4ClassRunner.class)(todo:what the diff with SpringRunner.class?)
@SpringBootTest(classes = BackendApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@WebMvcTest(UserController.class)
//@Transactional api层回滚无效，因为开启了server，server使用的独立db链接
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

    private final String HTTP_LOCALHOST = "http://localhost";


    protected abstract String getApiPrefix();

    protected String getHost(){
        return HTTP_LOCALHOST+":"+port+"/api";
    }



    protected  <T extends BaseAuditableEntity> void member_test_CRUD(T data) throws Exception {
        List<Runnable> tasks = new ArrayList<>();
        Map<String, Object> dataMap = JsonUtil.toMap(data);
        String id = dataMap.get("id").toString();

//        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
//

        try {
            // 在try块中定义任务
            DeleteDataHelper.addTask(() -> {
                getLog().info("Deleting data...");
                // 删除数据的实际逻辑
                getService().removeById(id);
            });

            //create
            ResultData resultData = given()
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


            assertThat(resultData).isNotNull();
            assertThat(resultData.getDataContent()).isNotNull();
            //read
            resultData = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+adminToken)
                    .when()
                    .get(getHost() + getApiPrefix() + "/find/"+id)
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);


            assertThat(resultData).isNotNull();
            String readId = ((Map<String, Object>) resultData.getDataContent()).get("id").toString();
            assertThat(readId).isEqualTo(id);

            //read all
            resultData = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+adminToken)
                    .when()
                    .queryParam("currentPage",1)
                    .queryParam("pageSize",200)
                    .get(getHost() + getApiPrefix() + "/findAll")
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);


            assertThat(resultData).isNotNull();
            PageInfo<Map<String,Object>> pageInfo = JsonUtil.fromMap((Map<String, Object>) resultData.getDataContent(), PageInfo.class);
            assertThat(pageInfo.getSize()).isGreaterThanOrEqualTo(3);
            var list = pageInfo.getList();
            Set<String> ids = list.stream().map(i -> i.get("id").toString()).collect(Collectors.toSet());
            assertThat(ids).contains(id);
            //read all
            resultData = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+adminToken)
                    .queryParam("currentPage",1)
                    .queryParam("pageSize",1)
                    .when()
                    .get(getHost() + getApiPrefix() + "/findAll")
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);


            assertThat(resultData).isNotNull();
            pageInfo = JsonUtil.fromMap((Map<String, Object>) resultData.getDataContent(), PageInfo.class);
            assertThat(pageInfo.getSize()).isEqualTo(1);
            assertThat(pageInfo.getPages()).isGreaterThanOrEqualTo(3);

            LocalDateTime aHourAgo = LocalDateTime.now().minusHours(1);
            data.setCreatedDate(aHourAgo.toString());

            //why use api to read but not service? service share same sqlSession, it will use it own cache,resulting in some reading error
            //read
            resultData = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+adminToken)
                    .when()
                    .get(getHost() + getApiPrefix() + "/find/"+id)
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);
            assertThat(resultData).isNotNull();
            var createdDate = ((Map<String, Object>) resultData.getDataContent()).get("createdDate").toString();
            assertThat(aHourAgo).isNotEqualTo(createdDate);

            //update
            resultData = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+adminToken)
                    .body(data)
                    .when()
                    .put(getHost() + getApiPrefix() + "/update/"+id)
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);


            assertThat(resultData).isNotNull();
            assertThat((Boolean) resultData.getDataContent()).isTrue();

            //read
            resultData = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+adminToken)
                    .when()
                    .get(getHost() + getApiPrefix() + "/find/"+id)
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);
            assertThat(resultData).isNotNull();
            var updatedCreateDate = ((Map<String, Object>) resultData.getDataContent()).get("createdDate").toString();
            assertThat(aHourAgo).isEqualTo(updatedCreateDate);

            //delete
            resultData = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+adminToken)
                    .body(data)
                    .when()
                    .delete(getHost() + getApiPrefix() + "/delete/"+id)
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);


            assertThat(resultData).isNotNull();
            assertThat((Boolean) resultData.getDataContent()).isTrue();
            clearMybatisLvl1Cache();

            //read
            resultData = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+adminToken)
                    .when()
                    .get(getHost() + getApiPrefix() + "/find/"+id)
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);
            assertThat(resultData).isNotNull();
            assertThat(resultData.getDataContent()).isNull();

        } finally {
            DeleteDataHelper.clear();
        }
    }


}

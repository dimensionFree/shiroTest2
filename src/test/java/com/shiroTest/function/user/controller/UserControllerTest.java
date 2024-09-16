package com.shiroTest.function.user.controller;

import com.shiroTest.common.ResultData;
import com.shiroTest.function.article.model.Article;
import com.shiroTest.function.article.service.impl.ArticleServiceImpl;
import com.shiroTest.function.base.BaseControllerTest;
import com.shiroTest.function.role.model.Authority;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.model.User4Display;
import com.shiroTest.function.user.model.UserLoginInfo;
import com.shiroTest.function.user.model.UserPwdDto;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import com.shiroTest.utils.DeleteDataHelper;
import com.shiroTest.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserControllerTest extends BaseControllerTest {

    @Override
    public UserServiceImpl getService() {
        return service;
    }

    @Override
    protected String getApiPrefix() {
        return "/user";
    }



    @Autowired
    public UserServiceImpl service;

    @Autowired
    public ArticleServiceImpl articleService;

    @Test
    @Transactional
    @Rollback
    public void crud_should_work() throws Exception {

        String inputUsername = "username";
        String inputPwd = "password";
        User data = new User(inputUsername, inputPwd);
        data.setId(UUID.randomUUID().toString());
        member_test_CRUD(data);

    }


    @Test
    public void testRegister() throws Exception {
        try {

            Article article = new Article();
            article.setContent("Hello,world!my dude.");
            assertEquals(article.getPreface(), "Hello,world!");

            boolean save = articleService.save(article);
            Article byId = articleService.getById(article.getId());
            assertEquals(byId.getPreface(), "Hello,world!");
            assertTrue(StringUtils.isNotBlank(byId.getCreatedBy()));
            assertNotNull(byId.getCreatedDate());
            assertTrue(StringUtils.isNotBlank(byId.getUpdatedBy()));
            assertNotNull(byId.getUpdatedDate());

            Article latestArticle = articleService.getLatestArticle();
            assertThat(latestArticle.getId()).isEqualTo(article.getId());


            String inputUsername = "username";
            String inputPwd = "password";
            String email = "test@a.com";
            // 生成验证码
            String verificationCode = getService().generateVerificationCode();

            // 将验证码和邮箱关联，并存储在临时存储（例如Redis），设置有效期
            getService().saveVerificationCode(email, verificationCode);
            var user = new UserPwdDto(inputUsername, inputPwd,email,verificationCode);

            //create
            ResultData resultData = given()
                    .header("Content-Type", "application/json")
                    .body(user)
                    .when()
                    .post(getHost() + getApiPrefix() + "/register")
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);

            assertThat(resultData).isNotNull();
            Map<String, Object> dataContent = (Map<String, Object>) resultData.getDataContent();
            UserLoginInfo userLoginInfo = JsonUtil.fromMap(dataContent, UserLoginInfo.class);
            var user4Display = userLoginInfo.getUser4Display();
            assertThat(user4Display).isNotNull();
            DeleteDataHelper.addTask(() -> {
                getLog().info("Deleting data...");
                // 删除数据的实际逻辑
                getService().removeById(user4Display.getId());
            });

            assertNotNull(user4Display.getRole());
            assertThat(user4Display.getRole().getAuthorities()).contains(Authority.ARTICLE_EDIT,Authority.ARTICLE_READ,Authority.USER_READ,Authority.USER_EDIT_SELF);
            assertThat(user4Display.getCreatedBy()).isEqualTo(user4Display.getId());
            var createDate = LocalDateTime.parse(user4Display.getCreatedDate());

            var now = LocalDateTime.now();
            assertThat(Math.abs(createDate.getYear()-now.getYear())).isLessThanOrEqualTo(1);
            assertThat(Math.abs(createDate.getMonthValue() - now.getMonthValue())).isLessThanOrEqualTo(1);
            assertThat(Math.abs(createDate.getDayOfMonth() - now.getDayOfMonth())).isLessThanOrEqualTo(1);
            assertThat(Math.abs(createDate.getHour() - now.getHour())).isLessThanOrEqualTo(1);
            assertThat(Math.abs(createDate.getMinute() - now.getMinute())).isLessThanOrEqualTo(1);
        }finally {
            DeleteDataHelper.clear();
        }
    }

    @Test
    public void testLogin() {
    }


}
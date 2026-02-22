package com.shiroTest.function.user.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.common.ResultData;
import com.shiroTest.function.base.BaseControllerTest;
import com.shiroTest.function.user.model.UserLoginInfo;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class UserSessionControllerTest extends BaseControllerTest {

    @Autowired
    private UserServiceImpl userService;

    @Override
    protected ServiceImpl getService() {
        return userService;
    }

    @Override
    protected String getApiPrefix() {
        return "/user";
    }

    @Test
    void refresh_should_issue_new_tokens_when_refresh_token_valid() throws Exception {
        UserLoginInfo loginInfo = userService.loginUser("admin", "adminPwd");
        assertThat(loginInfo.getRefreshToken()).isNotBlank();

        ResultData resultData = given()
                .header("Content-Type", "application/json")
                .body(Map.of("refreshToken", loginInfo.getRefreshToken()))
                .when()
                .post(getHost() + getApiPrefix() + "/refresh")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract()
                .response()
                .as(ResultData.class);

        assertThat(resultData.getDataContent()).isInstanceOf(Map.class);
        Map<String, Object> dataContent = (Map<String, Object>) resultData.getDataContent();
        assertThat(dataContent.get("token")).isNotNull();
        assertThat(dataContent.get("refreshToken")).isNotNull();
    }

    @Test
    void sessionPing_should_return_authenticated_when_token_valid() {
        ResultData resultData = given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(getHost() + getApiPrefix() + "/session/ping")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract()
                .response()
                .as(ResultData.class);

        assertThat(resultData.getDataContent()).isInstanceOf(Map.class);
        Map<String, Object> dataContent = (Map<String, Object>) resultData.getDataContent();
        assertThat(Boolean.parseBoolean(dataContent.get("authenticated").toString())).isTrue();
        assertThat(Long.parseLong(dataContent.get("tokenTtlSeconds").toString())).isGreaterThan(0L);
    }
}


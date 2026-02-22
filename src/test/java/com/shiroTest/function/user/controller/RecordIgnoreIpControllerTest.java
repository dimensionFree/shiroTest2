package com.shiroTest.function.user.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.common.ResultData;
import com.shiroTest.function.assistant.service.IRecordIgnoreIpService;
import com.shiroTest.function.base.BaseControllerTest;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class RecordIgnoreIpControllerTest extends BaseControllerTest {

    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private IRecordIgnoreIpService recordIgnoreIpService;

    @Override
    protected ServiceImpl getService() {
        return userService;
    }

    @Override
    protected String getApiPrefix() {
        return "/record/manage/ignore-ip";
    }

    @Test
    void addListRemove_should_work_for_admin() {
        String ip = "127.0.0.1";
        try {
            ResultData addResult = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + adminToken)
                    .body(Map.of("ip", ip))
                    .when()
                    .post(getHost() + getApiPrefix() + "/add")
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(ResultData.class);
            assertThat(Boolean.parseBoolean(addResult.getDataContent().toString())).isTrue();

            String listResponseText = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get(getHost() + getApiPrefix() + "/list")
                    .then()
                    .statusCode(200)
                    .extract()
                    .asString();
            assertThat(listResponseText).contains(ip);
        } finally {
            recordIgnoreIpService.removeIgnoredIp(ip);
        }
    }

    @Test
    void add_should_return_400_when_ip_invalid() {
        ResultData resultData = given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("ip", "bad-ip"))
                .when()
                .post(getHost() + getApiPrefix() + "/add")
                .then()
                .statusCode(400)
                .extract()
                .as(ResultData.class);

        assertThat(resultData.getMessage()).contains("invalid ip");
    }
}

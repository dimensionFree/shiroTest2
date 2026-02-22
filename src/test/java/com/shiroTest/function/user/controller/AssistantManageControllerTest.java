package com.shiroTest.function.user.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.common.ResultData;
import com.shiroTest.function.assistant.service.impl.AssistantInteractionRecordServiceImpl;
import com.shiroTest.function.base.BaseControllerTest;
import com.shiroTest.utils.DeleteDataHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class AssistantManageControllerTest extends BaseControllerTest {

    @Autowired
    private AssistantInteractionRecordServiceImpl assistantInteractionRecordService;

    @Override
    protected ServiceImpl getService() {
        return assistantInteractionRecordService;
    }

    @Override
    protected String getApiPrefix() {
        return "/assistant";
    }

    @Test
    void interactionManageRecords_should_return_paginated_data_for_admin() {
        try {
            String action = "message_send_" + System.currentTimeMillis();

            given()
                    .header("Content-Type", "application/json")
                    .body(Map.of(
                            "interactionType", "CHAT",
                            "interactionAction", action,
                            "interactionPayload", Map.of("message", "hello")
                    ))
                    .when()
                    .post(getHost() + getApiPrefix() + "/interaction")
                    .then()
                    .statusCode(200);

            ResultData resultData = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + adminToken)
                    .queryParam("currentPage", 1)
                    .queryParam("pageSize", 20)
                    .queryParam("interactionType", "CHAT")
                    .queryParam("interactionAction", action)
                    .queryParam("startDate", LocalDate.now().minusDays(7).toString())
                    .queryParam("endDate", LocalDate.now().toString())
                    .when()
                    .get(getHost() + getApiPrefix() + "/interaction/manage/records")
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);

            assertThat(resultData.getDataContent()).isInstanceOf(Map.class);
            Map<String, Object> pageInfo = (Map<String, Object>) resultData.getDataContent();
            assertThat(pageInfo.get("list")).isInstanceOf(List.class);
            List<Map<String, Object>> list = (List<Map<String, Object>>) pageInfo.get("list");
            assertThat(list).isNotEmpty();
            String id = list.get(0).get("id").toString();
            DeleteDataHelper.addTask(() -> assistantInteractionRecordService.removeById(id));
        } finally {
            DeleteDataHelper.clear();
        }
    }

    @Test
    void interactionManageRecords_should_return_400_when_currentPage_invalid() {
        ResultData resultData = given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("currentPage", 0)
                .queryParam("pageSize", 20)
                .when()
                .get(getHost() + getApiPrefix() + "/interaction/manage/records")
                .then()
                .statusCode(400)
                .contentType("application/json")
                .extract()
                .response()
                .as(ResultData.class);

        assertThat(resultData.getMessage()).contains("currentPage");
    }

    @Test
    void interactionManageRecords_should_return_400_when_date_range_invalid() {
        ResultData resultData = given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("currentPage", 1)
                .queryParam("pageSize", 20)
                .queryParam("startDate", "2026-02-10")
                .queryParam("endDate", "2026-02-01")
                .when()
                .get(getHost() + getApiPrefix() + "/interaction/manage/records")
                .then()
                .statusCode(400)
                .contentType("application/json")
                .extract()
                .response()
                .as(ResultData.class);

        assertThat(resultData.getMessage()).contains("startDate cannot be after endDate");
    }

    @Test
    void interactionManageRecords_should_auto_flush_when_flag_enabled() {
        String action = "flush_tap_" + System.currentTimeMillis();
        try {
            given()
                    .header("Content-Type", "application/json")
                    .body(Map.of(
                            "interactionType", "AVATAR",
                            "interactionAction", action,
                            "interactionPayload", Map.of("kind", "tap")
                    ))
                    .when()
                    .post(getHost() + getApiPrefix() + "/interaction")
                    .then()
                    .statusCode(200);

            ResultData queryResult = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + adminToken)
                    .queryParam("currentPage", 1)
                    .queryParam("pageSize", 20)
                    .queryParam("interactionType", "AVATAR")
                    .queryParam("interactionAction", action)
                    .queryParam("startDate", LocalDate.now().minusDays(1).toString())
                    .queryParam("endDate", LocalDate.now().toString())
                    .queryParam("autoFlush", true)
                    .when()
                    .get(getHost() + getApiPrefix() + "/interaction/manage/records")
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);

            assertThat(queryResult.getDataContent()).isInstanceOf(Map.class);
            Map<String, Object> pageInfo = (Map<String, Object>) queryResult.getDataContent();
            assertThat(pageInfo.get("list")).isInstanceOf(List.class);
            List<Map<String, Object>> list = (List<Map<String, Object>>) pageInfo.get("list");
            assertThat(list).isNotEmpty();
            for (Map<String, Object> item : list) {
                if (item.get("id") != null) {
                    String id = item.get("id").toString();
                    DeleteDataHelper.addTask(() -> assistantInteractionRecordService.removeById(id));
                }
            }
        } finally {
            DeleteDataHelper.clear();
        }
    }
}

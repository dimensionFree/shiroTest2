package com.shiroTest.function.assistant.controller;

import com.shiroTest.BackendApplication;
import com.shiroTest.function.assistant.model.AssistantContextResponse;
import com.shiroTest.function.assistant.service.IAssistantService;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = BackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AssistantControllerTest {

    @LocalServerPort
    private int port;

    @MockBean
    private IAssistantService assistantService;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void getContext_should_return_200_and_result_structure() {
        when(assistantService.getContextByIp(anyString()))
                .thenReturn(new AssistantContextResponse("Tokyo", 35.68, 139.76, 1, 22.5));

        Map<String, Object> body = given()
                .header("Content-Type", "application/json")
                .when()
                .get("/api/assistant/context")
                .then()
                .statusCode(200)
                .extract()
                .as(Map.class);

        assertThat(body.get("code")).isEqualTo("200");
        assertThat(body.get("dataContent")).isInstanceOf(Map.class);
        Map<String, Object> dataContent = (Map<String, Object>) body.get("dataContent");
        assertThat(dataContent.get("city")).isEqualTo("Tokyo");
        assertThat(dataContent.get("weatherCode")).isEqualTo(1);
    }

    @Test
    void getContext_when_service_throw_should_map_to_400() {
        when(assistantService.getContextByIp(anyString()))
                .thenThrow(new RuntimeException("downstream failed"));

        Map<String, Object> body = given()
                .header("Content-Type", "application/json")
                .when()
                .get("/api/assistant/context")
                .then()
                .statusCode(400)
                .extract()
                .as(Map.class);

        assertThat(body.get("message")).isEqualTo("downstream failed");
    }
}

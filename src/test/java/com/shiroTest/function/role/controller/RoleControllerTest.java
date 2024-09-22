package com.shiroTest.function.role.controller;

import com.github.pagehelper.PageInfo;
import com.shiroTest.common.ResultData;
import com.shiroTest.function.base.BaseControllerTest;
import com.shiroTest.function.role.model.Authority;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.model.User4Display;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import com.shiroTest.utils.DeleteDataHelper;
import com.shiroTest.utils.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.shiroTest.function.role.service.impl.RoleServiceImpl.ROLE_ID_ADMIN;
import static com.shiroTest.function.role.service.impl.RoleServiceImpl.ROLE_ID_MEMBER;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RoleControllerTest extends BaseControllerTest {

    @Override
    public UserServiceImpl getService() {
        return service;
    }

    @Override
    protected String getApiPrefix() {
        return "/role";
    }



    @Autowired
    public UserServiceImpl service;


    @Test
    @Transactional
    @Rollback
    void findAllRole_should_work(){
        //read all
        var resultData = given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer "+adminToken)
//                .queryParam("currentPage",1)
//                .queryParam("pageSize",1)
                .when()
                .get(getHost() + getApiPrefix() + "/findAll")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract()
                .response()
                .as(ResultData.class);

        assertThat(resultData).isNotNull();
        PageInfo<Map<String,Object>> pageInfo = JsonUtil.fromMap((Map<String, Object>) resultData.getDataContent(), PageInfo.class);
        var list = pageInfo.getList();
        assertThat(list).isNotEmpty();
        assertThat(list).hasSize(2);
        assertThat(list.stream().map(i->i.get("id")).collect(Collectors.toSet())).contains(ROLE_ID_ADMIN,ROLE_ID_MEMBER);

    }


}
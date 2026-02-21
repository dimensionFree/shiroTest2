package com.shiroTest.function.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.common.ResultData;
import com.shiroTest.function.article.model.Article;
import com.shiroTest.function.article.model.ArticleReadRecord;
import com.shiroTest.function.article.service.impl.ArticleReadRecordServiceImpl;
import com.shiroTest.function.article.service.impl.ArticleServiceImpl;
import com.shiroTest.function.base.BaseControllerTest;
import com.shiroTest.utils.DeleteDataHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class ArticleControllerTest extends BaseControllerTest {

    @Autowired
    private ArticleServiceImpl articleService;

    @Autowired
    private ArticleReadRecordServiceImpl articleReadRecordService;

    @Override
    protected ServiceImpl getService() {
        return articleService;
    }

    @Override
    protected String getApiPrefix() {
        return "/article";
    }

    private String createArticle(Boolean isPublic) {
        Article article = new Article();
        article.setTitle("article for test");
        article.setContent("content");
        article.setIsPublic(isPublic);
        ResultData createResult = given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(article)
                .when()
                .post(getHost() + getApiPrefix() + "/create")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract()
                .response()
                .as(ResultData.class);
        String articleId = createResult.getDataContent().toString();
        DeleteDataHelper.addTask(() -> articleService.removeById(articleId));
        return articleId;
    }

    @Test
    void findById_should_not_record_read_log() {
        try {
            // Given
            String articleId = createArticle(true);

            // When
            ResultData resultData = given()
                    .header("Content-Type", "application/json")
                    .header("X-Forwarded-For", "9.8.7.6, 10.0.0.1")
                    .header("User-Agent", "ArticleReadRecordTestAgent/1.0")
                    .when()
                    .get(getHost() + getApiPrefix() + "/find/" + articleId)
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);

            // Then
            assertThat(resultData).isNotNull();
            assertThat(resultData.getDataContent()).isInstanceOf(Map.class);
            String returnedId = ((Map<String, Object>) resultData.getDataContent()).get("id").toString();
            assertThat(returnedId).isEqualTo(articleId);

            clearMybatisLvl1Cache();
            List<ArticleReadRecord> records = articleReadRecordService.list(
                    new QueryWrapper<ArticleReadRecord>()
                            .eq("article_id", articleId)
                            .orderByDesc("read_time")
            );
            assertThat(records).isEmpty();
        } finally {
            DeleteDataHelper.clear();
        }
    }

    @Test
    void findById_withRecordReadTrue_should_record_read_log_with_client_ip_and_time() {
        try {
            // Given
            String articleId = createArticle(true);

            // When
            ResultData resultData = given()
                    .header("Content-Type", "application/json")
                    .header("X-Forwarded-For", "9.8.7.6, 10.0.0.1")
                    .header("User-Agent", "ArticleReadRecordTestAgent/1.0")
                    .when()
                    .get(getHost() + getApiPrefix() + "/find/" + articleId + "?recordRead=true")
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);

            // Then
            assertThat(resultData).isNotNull();
            assertThat(resultData.getDataContent()).isInstanceOf(Map.class);
            String returnedId = ((Map<String, Object>) resultData.getDataContent()).get("id").toString();
            assertThat(returnedId).isEqualTo(articleId);

            clearMybatisLvl1Cache();
            List<ArticleReadRecord> records = articleReadRecordService.list(
                    new QueryWrapper<ArticleReadRecord>()
                            .eq("article_id", articleId)
                            .orderByDesc("read_time")
            );
            assertThat(records).isNotEmpty();
            assertThat(records.get(0).getReaderIp()).isEqualTo("9.8.7.6");
            assertThat(records.get(0).getReadTime()).isNotNull();
        } finally {
            DeleteDataHelper.clear();
        }
    }

    @Test
    void findById_should_return_400_when_id_blank() {
        ResultData resultData = given()
                .header("Content-Type", "application/json")
                .when()
                .get(getHost() + getApiPrefix() + "/find/%20")
                .then()
                .statusCode(400)
                .contentType("application/json")
                .extract()
                .response()
                .as(ResultData.class);

        assertThat(resultData).isNotNull();
        assertThat(resultData.getMessage()).contains("article id format invalid");
    }

    @Test
    void privateArticle_should_not_be_visible_for_anonymous() {
        try {
            String articleId = createArticle(false);

            ResultData resultData = given()
                    .header("Content-Type", "application/json")
                    .when()
                    .get(getHost() + getApiPrefix() + "/find/" + articleId)
                    .then()
                    .statusCode(400)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);

            assertThat(resultData.getMessage()).contains("article is not public");
        } finally {
            DeleteDataHelper.clear();
        }
    }

    @Test
    void privateArticle_should_be_visible_for_authenticated_user() {
        try {
            String articleId = createArticle(false);

            ResultData resultData = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get(getHost() + getApiPrefix() + "/manage/find/" + articleId)
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);

            assertThat(resultData.getDataContent()).isInstanceOf(Map.class);
            assertThat(((Map<String, Object>) resultData.getDataContent()).get("id").toString()).isEqualTo(articleId);
        } finally {
            DeleteDataHelper.clear();
        }
    }

    @Test
    void readDetail_should_return_stats_for_admin_page() {
        try {
            String articleId = createArticle(true);

            given().header("Content-Type", "application/json")
                    .header("X-Forwarded-For", "1.1.1.1")
                    .when()
                    .get(getHost() + getApiPrefix() + "/find/" + articleId + "?recordRead=true")
                    .then()
                    .statusCode(200);

            given().header("Content-Type", "application/json")
                    .header("X-Forwarded-For", "2.2.2.2")
                    .when()
                    .get(getHost() + getApiPrefix() + "/find/" + articleId + "?recordRead=true")
                    .then()
                    .statusCode(200);

            ResultData detailResult = given()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + adminToken)
                    .queryParam("recordLimit", 20)
                    .queryParam("dayLimit", 7)
                    .when()
                    .get(getHost() + getApiPrefix() + "/read/detail/" + articleId)
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response()
                    .as(ResultData.class);

            assertThat(detailResult.getDataContent()).isInstanceOf(Map.class);
            Map<String, Object> detail = (Map<String, Object>) detailResult.getDataContent();
            assertThat(detail.get("articleId").toString()).isEqualTo(articleId);
            assertThat(Long.parseLong(detail.get("totalReadCount").toString())).isGreaterThanOrEqualTo(2L);
            assertThat(Long.parseLong(detail.get("uniqueIpCount").toString())).isGreaterThanOrEqualTo(2L);
            assertThat((List<?>) detail.get("latestRecords")).isNotEmpty();
        } finally {
            DeleteDataHelper.clear();
        }
    }

    @Test
    void managePublic_should_toggle_article_visibility() {
        try {
            String articleId = createArticle(true);

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .patch(getHost() + getApiPrefix() + "/manage/public/" + articleId + "?isPublic=false")
                    .then()
                    .statusCode(200);

            given()
                    .when()
                    .get(getHost() + getApiPrefix() + "/find/" + articleId)
                    .then()
                    .statusCode(400);

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .patch(getHost() + getApiPrefix() + "/manage/public/" + articleId + "?isPublic=true")
                    .then()
                    .statusCode(200);

            given()
                    .when()
                    .get(getHost() + getApiPrefix() + "/find/" + articleId)
                    .then()
                    .statusCode(200);
        } finally {
            DeleteDataHelper.clear();
        }
    }
}

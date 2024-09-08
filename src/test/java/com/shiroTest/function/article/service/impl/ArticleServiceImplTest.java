package com.shiroTest.function.article.service.impl;

import com.shiroTest.function.article.model.Article;
import com.shiroTest.function.base.BaseServiceTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


public class ArticleServiceImplTest extends BaseServiceTest {


    @Autowired
    ArticleServiceImpl service;
    @Test
    public void save_should_get_preface() {
        Article article = new Article();
        article.setContent("Hello,world!my dude.");
        assertEquals(article.getPreface(), "Hello,world!");

        boolean save = service.save(article);
        Article byId = service.getById(article.getId());
        assertEquals(byId.getPreface(), "Hello,world!");
        assertTrue(StringUtils.isNotBlank(byId.getCreatedBy()));
        assertNotNull(byId.getCreatedDate());
        assertTrue(StringUtils.isNotBlank(byId.getUpdatedBy()));
        assertNotNull(byId.getUpdatedDate());

        Article latestArticle = service.getLatestArticle();
        assertThat(latestArticle.getId()).isEqualTo(article.getId());



    }

}
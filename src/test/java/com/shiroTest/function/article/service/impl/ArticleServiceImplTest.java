package com.shiroTest.function.article.service.impl;

import com.shiroTest.BackendApplication;
import com.shiroTest.function.article.model.Article;
import com.shiroTest.function.role.dao.RoleMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApplication.class)
public class ArticleServiceImplTest{


    @Autowired
    ArticleServiceImpl service;


    @Test
    public void save_should_get_preface(){
        Article article = new Article();
        article.setContent("Hello,world!my dude.");
        Assert.assertEquals(article.getPreface(),"Hello,world!");

        try {
            boolean save = service.save(article);
            Article byId = service.getById(article.getId());
            Assert.assertEquals(byId.getPreface(),"Hello,world!");
            Assert.assertTrue(StringUtils.isNotBlank( byId.getCreateBy()));
            Assert.assertNotNull(byId.getCreateDate());
            Assert.assertTrue(StringUtils.isNotBlank( byId.getUpdateBy()));
            Assert.assertNotNull(byId.getUpdateDate());
        } finally {
            service.removeById(article.getId());
        }

    }
}
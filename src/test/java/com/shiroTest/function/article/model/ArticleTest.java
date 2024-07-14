package com.shiroTest.function.article.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ArticleTest {

    @Test
    public void get_preface_should_work(){
        Article article = new Article();
        article.setContent("Hello,world!my dude.");
        assertEquals(article.getPreface(),"Hello,world!");
    }
}
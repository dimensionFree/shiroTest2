package com.shiroTest.function.article.model;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Test;

public class ArticleTest {

    @Test
    public void get_preface_should_work(){
        Article article = new Article();
        article.setContent("Hello,world!my dude.");
        assertthat
        Assert.assertEquals(article.getPreface(),"Hello,world!");
    }
}
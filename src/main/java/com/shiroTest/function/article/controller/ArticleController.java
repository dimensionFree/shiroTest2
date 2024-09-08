package com.shiroTest.function.article.controller;


import com.shiroTest.common.Result;
import com.shiroTest.function.article.model.Article;
import com.shiroTest.function.article.service.impl.ArticleServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.shiroTest.function.base.BaseController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author freedom
 * @since 2023-09-04
 */
@RestController
@RequestMapping("/article")
public class ArticleController extends BaseController<Article, ArticleServiceImpl> {

    public ArticleController() {
        super(Article.class);
    }

    @GetMapping("/read/latest")
    public Result getLatestArticle(){
        return Result.success(getService().getLatestArticle());
    }
}


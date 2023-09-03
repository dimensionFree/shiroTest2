package com.shiroTest.article.controller;


import com.shiroTest.article.model.Article;
import com.shiroTest.article.service.IArticleService;
import com.shiroTest.article.service.impl.ArticleServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.shiroTest.base.BaseController;

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

}


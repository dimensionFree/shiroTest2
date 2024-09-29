package com.shiroTest.function.article.controller;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shiroTest.common.Result;
import com.shiroTest.function.article.model.Article;
import com.shiroTest.function.article.model.ArticleDto;
import com.shiroTest.function.article.service.impl.ArticleServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.shiroTest.function.base.BaseController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author freedom
 * @since 2023-09-04
 */
@RestController
@RequestMapping("/api/article")
public class ArticleController extends BaseController<Article, ArticleServiceImpl> {

    public ArticleController() {
        super(Article.class);
    }

    @GetMapping("/read/latest")
    public Result getLatestArticle(){
        return Result.success(getService().getLatestArticle());
    }

    @Override
    public Result getAll(int currentPage, int pageSize) {
        // 开始分页
        PageHelper.startPage(currentPage, pageSize,"created_date DESC");
        List<ArticleDto> list = getService().listDto();

        // 获取分页信息
        PageInfo<ArticleDto> pageInfo = new PageInfo<>(list);

//        // 在分页信息基础上进行处理
//        List beforeReturnList = beforeReturnList(list);
        pageInfo.setList(list);

        return Result.success(pageInfo);



    }
}


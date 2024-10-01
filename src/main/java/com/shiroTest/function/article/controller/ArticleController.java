package com.shiroTest.function.article.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shiroTest.common.Result;
import com.shiroTest.function.article.model.Article;
import com.shiroTest.function.article.model.ArticleDto;
import com.shiroTest.function.article.service.impl.ArticleServiceImpl;
import com.shiroTest.function.base.FilterWrapper;
import com.shiroTest.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.shiroTest.function.base.BaseController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    public Result getAll(int currentPage, int pageSize, String sortBy, Boolean ascending, String filtersStr) throws IOException {



        // 开始分页
        PageHelper.startPage(currentPage, pageSize,"created_date DESC");

        // 构建查询条件
        QueryWrapper<ArticleDto> queryWrapper = new QueryWrapper<>();
        // 根据前端传递的过滤条件进行筛选

        if (StringUtils.isNotEmpty(filtersStr)) {
            Map<String, Object> filters = JsonUtil.toMap(filtersStr);
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                queryWrapper.eq("a."+ entry.getKey(), entry.getValue().toString());
            }
        }


//        // 添加排序条件
//        if (sortBy != null) {
//            if (Boolean.TRUE.equals(ascending)) {
//                queryWrapper.orderByAsc(sortBy);
//            } else {
//                queryWrapper.orderByDesc(sortBy);
//            }
//        }


        List<ArticleDto> list = getService().listDto(queryWrapper);

        // 获取分页信息
        PageInfo<ArticleDto> pageInfo = new PageInfo<>(list);

//        // 在分页信息基础上进行处理
//        List beforeReturnList = beforeReturnList(list);
        pageInfo.setList(list);

        return Result.success(pageInfo);

    }


    @Override
    public Result getById(@PathVariable("id") String id) {
        ArticleDto byId = getService().getDtoById(id);
//        var t = beforeReturn(byId);
        return  Result.success(byId);
    }
}


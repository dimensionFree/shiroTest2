package com.shiroTest.function.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.function.article.dao.ArticleMapper;
import com.shiroTest.function.article.model.Article;
import com.shiroTest.function.article.model.ArticleDto;
import com.shiroTest.function.article.service.IArticleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author freedom
 * @since 2023-09-04
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {

    public Article getLatestArticle() {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_date").last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean save(Article entity) {
        return super.save(entity);
    }



//    public List<ArticleDto> listDto() {
//        return getBaseMapper().selectArticleDto();
//    }

    public List<ArticleDto> listDto(QueryWrapper<ArticleDto> queryWrapper) {
        return getBaseMapper().selectArticleDto(queryWrapper);
    }


    public ArticleDto getDtoById(String id) {
        return getBaseMapper().selectArticleDtoById(id);

    }
}

package com.shiroTest.function.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.function.article.dao.ArticleMapper;
import com.shiroTest.function.article.model.Article;
import com.shiroTest.function.article.service.IArticleService;
import org.springframework.stereotype.Service;

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
        return null;
    }
}

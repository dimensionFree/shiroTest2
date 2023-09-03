package com.shiroTest.article.dao;

import com.shiroTest.article.model.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author freedom
 * @since 2023-09-04
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

}

package com.shiroTest.function.article.dao;

import com.shiroTest.function.article.model.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiroTest.function.article.model.ArticleDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    @Select("SELECT a.*,user.username as createdUserName from article a , user WHERE a.created_by=user.id")
     public List<ArticleDto> selectArticleDto();
}

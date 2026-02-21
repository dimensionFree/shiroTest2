package com.shiroTest.function.article.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiroTest.function.article.model.ArticleReadDailyStat;
import com.shiroTest.function.article.model.ArticleReadRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleReadRecordMapper extends BaseMapper<ArticleReadRecord> {

    @Select("SELECT COUNT(DISTINCT reader_ip) FROM article_read_record WHERE article_id = #{articleId}")
    Long countDistinctReaderIp(@Param("articleId") String articleId);

    @Select("SELECT DATE(read_time) as readDate, COUNT(*) as readCount " +
            "FROM article_read_record WHERE article_id = #{articleId} " +
            "GROUP BY DATE(read_time) ORDER BY DATE(read_time) DESC LIMIT #{dayLimit}")
    List<ArticleReadDailyStat> selectDailyStats(@Param("articleId") String articleId, @Param("dayLimit") int dayLimit);

    @Select("SELECT * FROM article_read_record WHERE article_id = #{articleId} ORDER BY read_time DESC LIMIT #{recordLimit}")
    List<ArticleReadRecord> selectLatestRecords(@Param("articleId") String articleId, @Param("recordLimit") int recordLimit);
}

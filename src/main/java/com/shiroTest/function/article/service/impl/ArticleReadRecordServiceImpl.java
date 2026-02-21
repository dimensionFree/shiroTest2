package com.shiroTest.function.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.function.article.model.ArticleReadDetailResponse;
import com.shiroTest.function.article.dao.ArticleReadRecordMapper;
import com.shiroTest.function.article.model.ArticleReadRecord;
import com.shiroTest.function.article.service.IArticleReadRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ArticleReadRecordServiceImpl extends ServiceImpl<ArticleReadRecordMapper, ArticleReadRecord> implements IArticleReadRecordService {

    @Override
    public void recordRead(String articleId, String readerIp, String readerUserId, String readerUserAgent) {
        if (StringUtils.isBlank(articleId)) {
            throw new IllegalArgumentException("articleId cannot be blank");
        }
        if (StringUtils.isBlank(readerIp)) {
            throw new IllegalArgumentException("readerIp cannot be blank");
        }

        ArticleReadRecord record = new ArticleReadRecord();
        record.setArticleId(articleId);
        record.setReaderIp(readerIp);
        record.setReaderUserId(StringUtils.trimToNull(readerUserId));
        record.setReaderUserAgent(StringUtils.trimToNull(readerUserAgent));
        record.setReadTime(LocalDateTime.now());
        save(record);
    }

    @Override
    public ArticleReadDetailResponse getReadDetail(String articleId, int recordLimit, int dayLimit) {
        if (StringUtils.isBlank(articleId)) {
            throw new IllegalArgumentException("articleId cannot be blank");
        }
        int safeRecordLimit = Math.max(1, Math.min(recordLimit, 500));
        int safeDayLimit = Math.max(1, Math.min(dayLimit, 90));

        ArticleReadDetailResponse response = new ArticleReadDetailResponse();
        response.setArticleId(articleId);
        response.setTotalReadCount((long) count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ArticleReadRecord>().eq("article_id", articleId)));
        Long uniqueIpCount = getBaseMapper().countDistinctReaderIp(articleId);
        response.setUniqueIpCount(uniqueIpCount == null ? 0L : uniqueIpCount);
        response.setDailyStats(getBaseMapper().selectDailyStats(articleId, safeDayLimit));
        response.setLatestRecords(getBaseMapper().selectLatestRecords(articleId, safeRecordLimit));
        return response;
    }
}

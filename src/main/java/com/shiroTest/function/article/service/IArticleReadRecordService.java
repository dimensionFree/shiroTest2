package com.shiroTest.function.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiroTest.function.article.model.ArticleReadDetailResponse;
import com.shiroTest.function.article.model.ArticleReadRecord;

public interface IArticleReadRecordService extends IService<ArticleReadRecord> {

    void recordRead(String articleId, String readerIp, String readerUserId, String readerUserAgent);

    ArticleReadDetailResponse getReadDetail(String articleId, int recordLimit, int dayLimit);
}

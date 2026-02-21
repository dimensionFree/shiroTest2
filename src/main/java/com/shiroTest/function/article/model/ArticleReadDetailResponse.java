package com.shiroTest.function.article.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ArticleReadDetailResponse {
    private String articleId;
    private Long totalReadCount;
    private Long uniqueIpCount;
    private List<ArticleReadDailyStat> dailyStats = new ArrayList<>();
    private List<ArticleReadRecord> latestRecords = new ArrayList<>();
}

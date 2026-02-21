package com.shiroTest.function.article.model;

import com.shiroTest.function.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleReadRecord extends BaseEntity {

    private String articleId;

    private String readerIp;

    private String readerUserId;

    private String readerUserAgent;

    private LocalDateTime readTime;
}

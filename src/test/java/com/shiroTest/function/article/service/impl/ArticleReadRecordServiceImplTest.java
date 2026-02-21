package com.shiroTest.function.article.service.impl;

import com.shiroTest.function.article.dao.ArticleReadRecordMapper;
import com.shiroTest.function.article.model.ArticleReadDailyStat;
import com.shiroTest.function.article.model.ArticleReadDetailResponse;
import com.shiroTest.function.article.model.ArticleReadRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ArticleReadRecordServiceImplTest {

    @Mock
    private ArticleReadRecordMapper articleReadRecordMapper;

    private ArticleReadRecordServiceImpl articleReadRecordService;

    @BeforeEach
    void setUp() {
        articleReadRecordService = new ArticleReadRecordServiceImpl();
        ReflectionTestUtils.setField(articleReadRecordService, "baseMapper", articleReadRecordMapper);
    }

    @Test
    void recordRead_should_save_record_when_input_valid() {
        // Given
        String articleId = "article-id-1";
        String readerIp = "1.2.3.4";

        // When
        articleReadRecordService.recordRead(articleId, readerIp, "user-id-1", "Mozilla");

        // Then
        verify(articleReadRecordMapper).insert(any(ArticleReadRecord.class));
    }

    @Test
    void recordRead_should_throw_when_articleId_blank() {
        // Given / When / Then
        assertThatThrownBy(() -> articleReadRecordService.recordRead(" ", "1.2.3.4", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("articleId");
    }

    @Test
    void recordRead_should_throw_when_mapper_fails() {
        // Given
        doThrow(new RuntimeException("db error")).when(articleReadRecordMapper).insert(any(ArticleReadRecord.class));

        // When / Then
        assertThatThrownBy(() -> articleReadRecordService.recordRead("article-id-2", "2.2.2.2", null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db error");
    }

    @Test
    void getReadDetail_should_return_summary_and_records() {
        // Given
        ArticleReadDailyStat dailyStat = new ArticleReadDailyStat();
        dailyStat.setReadDate("2026-02-21");
        dailyStat.setReadCount(5);
        doReturn(2).when(articleReadRecordMapper).selectCount(any());
        doReturn(1L).when(articleReadRecordMapper).countDistinctReaderIp("article-id-3");
        doReturn(java.util.List.of(dailyStat)).when(articleReadRecordMapper).selectDailyStats("article-id-3", 30);
        doReturn(java.util.List.of(new ArticleReadRecord())).when(articleReadRecordMapper).selectLatestRecords("article-id-3", 100);

        // When
        ArticleReadDetailResponse response = articleReadRecordService.getReadDetail("article-id-3", 100, 30);

        // Then
        assertThat(response.getArticleId()).isEqualTo("article-id-3");
        assertThat(response.getTotalReadCount()).isEqualTo(2L);
        assertThat(response.getUniqueIpCount()).isEqualTo(1L);
        assertThat(response.getDailyStats()).hasSize(1);
        assertThat(response.getLatestRecords()).hasSize(1);
    }
}

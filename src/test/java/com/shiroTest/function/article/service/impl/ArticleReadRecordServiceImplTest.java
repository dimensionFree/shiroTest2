package com.shiroTest.function.article.service.impl;

import com.shiroTest.function.article.dao.ArticleReadRecordMapper;
import com.shiroTest.function.article.model.ArticleReadDailyStat;
import com.shiroTest.function.article.model.ArticleReadDetailResponse;
import com.shiroTest.function.article.model.ArticleReadRecord;
import com.shiroTest.function.assistant.model.GeoContext;
import com.shiroTest.function.assistant.service.AssistantRemoteClient;
import com.shiroTest.function.assistant.service.IRecordIgnoreIpService;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class ArticleReadRecordServiceImplTest {

    @Mock
    private ArticleReadRecordMapper articleReadRecordMapper;

    @Mock
    private AssistantRemoteClient assistantRemoteClient;
    @Mock
    private IRecordIgnoreIpService recordIgnoreIpService;

    private ArticleReadRecordServiceImpl articleReadRecordService;

    @BeforeEach
    void setUp() {
        articleReadRecordService = new ArticleReadRecordServiceImpl();
        ReflectionTestUtils.setField(articleReadRecordService, "baseMapper", articleReadRecordMapper);
        ReflectionTestUtils.setField(articleReadRecordService, "assistantRemoteClient", assistantRemoteClient);
        ReflectionTestUtils.setField(articleReadRecordService, "recordIgnoreIpService", recordIgnoreIpService);
        lenient().when(recordIgnoreIpService.shouldIgnore(any())).thenReturn(false);
    }

    @Test
    void recordRead_should_save_record_when_input_valid() {
        // Given
        String articleId = "article-id-1";
        String readerIp = "1.2.3.4";
        doReturn(new GeoContext("Japan", "Tokyo", "Shinjuku", null, null)).when(assistantRemoteClient).fetchGeoContext(readerIp);

        // When
        articleReadRecordService.recordRead(articleId, readerIp, "user-id-1", "Mozilla");

        // Then
        ArgumentCaptor<ArticleReadRecord> captor = ArgumentCaptor.forClass(ArticleReadRecord.class);
        verify(articleReadRecordMapper).insert(captor.capture());
        assertThat(captor.getValue().getReaderIpCountry()).isEqualTo("Japan");
        assertThat(captor.getValue().getReaderIpProvince()).isEqualTo("Tokyo");
        assertThat(captor.getValue().getReaderIpCity()).isEqualTo("Shinjuku");
        assertThat(captor.getValue().getReaderIpLocation()).isEqualTo("Shinjuku");
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
        doReturn(new GeoContext("Japan", "Osaka", "Kita", null, null)).when(assistantRemoteClient).fetchGeoContext("2.2.2.2");

        // When / Then
        assertThatThrownBy(() -> articleReadRecordService.recordRead("article-id-2", "2.2.2.2", null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db error");
    }

    @Test
    void recordRead_should_mark_private_network_for_private_ip() {
        // Given
        String articleId = "article-id-4";
        String readerIp = "192.168.1.5";

        // When
        articleReadRecordService.recordRead(articleId, readerIp, null, null);

        // Then
        ArgumentCaptor<ArticleReadRecord> captor = ArgumentCaptor.forClass(ArticleReadRecord.class);
        verify(articleReadRecordMapper).insert(captor.capture());
        assertThat(captor.getValue().getReaderIpCountry()).isEqualTo("PRIVATE_NETWORK");
        assertThat(captor.getValue().getReaderIpProvince()).isEqualTo("PRIVATE_NETWORK");
        assertThat(captor.getValue().getReaderIpCity()).isEqualTo("PRIVATE_NETWORK");
        assertThat(captor.getValue().getReaderIpLocation()).isEqualTo("PRIVATE_NETWORK");
        verify(assistantRemoteClient, never()).fetchGeoContext(any());
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

    @Test
    void getManageRecords_should_return_page_info() {
        doReturn(java.util.List.of(new ArticleReadRecord(), new ArticleReadRecord()))
                .when(articleReadRecordMapper)
                .selectList(any());

        PageInfo<ArticleReadRecord> pageInfo = articleReadRecordService.getManageRecords(
                1,
                20,
                "article-id-5",
                LocalDate.now().minusDays(7),
                LocalDate.now()
        );

        assertThat(pageInfo).isNotNull();
        assertThat(pageInfo.getList()).hasSize(2);
    }

    @Test
    void recordRead_should_skip_when_ip_ignored() {
        when(recordIgnoreIpService.shouldIgnore("10.10.10.10")).thenReturn(true);

        articleReadRecordService.recordRead("article-id-skip", "10.10.10.10", null, null);

        verify(articleReadRecordMapper, never()).insert(any(ArticleReadRecord.class));
        verify(assistantRemoteClient, never()).fetchGeoContext(any());
    }
}

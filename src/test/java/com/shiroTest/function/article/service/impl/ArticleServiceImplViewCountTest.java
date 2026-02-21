package com.shiroTest.function.article.service.impl;

import com.shiroTest.function.article.dao.ArticleMapper;
import com.shiroTest.function.article.model.ArticleDto;
import com.shiroTest.utils.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplViewCountTest {

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private RedisUtil redisUtil;

    private ArticleServiceImpl articleService;

    @BeforeEach
    void setUp() {
        articleService = new ArticleServiceImpl();
        ReflectionTestUtils.setField(articleService, "baseMapper", articleMapper);
        ReflectionTestUtils.setField(articleService, "redisUtil", redisUtil);
    }

    @Test
    void getDtoById_should_not_update_view_count_when_flag_false() {
        // Given
        ArticleDto dto = new ArticleDto();
        dto.setId("article-id-1");
        dto.setViewCount(3);
        when(articleMapper.selectArticleDtoById("article-id-1")).thenReturn(dto);

        // When
        articleService.getDtoById("article-id-1", false);

        // Then
        verify(redisUtil, never()).hasKey("VIEW_COUNT_article-id-1");
        verify(redisUtil, never()).incr("VIEW_COUNT_article-id-1", 1);
    }

    @Test
    void getDtoById_should_update_view_count_when_flag_true() {
        // Given
        ArticleDto dto = new ArticleDto();
        dto.setId("article-id-2");
        dto.setViewCount(8);
        when(articleMapper.selectArticleDtoById("article-id-2")).thenReturn(dto);
        when(redisUtil.hasKey("VIEW_COUNT_article-id-2")).thenReturn(true);

        // When
        articleService.getDtoById("article-id-2", true);

        // Then
        verify(redisUtil).hasKey("VIEW_COUNT_article-id-2");
        verify(redisUtil).incr("VIEW_COUNT_article-id-2", 1);
    }
}


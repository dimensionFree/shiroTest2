package com.shiroTest.utils;

import com.shiroTest.function.base.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static com.shiroTest.function.article.service.impl.ArticleServiceImpl.REDIS_VIEW_COUNT_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RedisUtilTest extends BaseTest {
    @Autowired
    RedisUtil redisUtil;

    @Test
    void keys_should_work(){
        String key = REDIS_VIEW_COUNT_PREFIX + "articleId";
        redisUtil.set(key,2);
        Set<String> keys = redisUtil.keys(REDIS_VIEW_COUNT_PREFIX + "*");
        assertThat(keys).contains(key);

    }

}
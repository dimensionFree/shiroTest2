package com.shiroTest.function.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.function.article.dao.ArticleMapper;
import com.shiroTest.function.article.model.Article;
import com.shiroTest.function.article.model.ArticleDto;
import com.shiroTest.function.article.service.IArticleService;
import com.shiroTest.utils.RedisUtil;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author freedom
 * @since 2023-09-04
 */
@Service
@Log4j2
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {

    public static final String REDIS_VIEW_COUNT_PREFIX = "VIEW_COUNT_";
    @Autowired
    RedisUtil redisUtil;

    public Article getLatestArticle() {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_date").last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    public Article getLatestPublicArticle() {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_public", 1).orderByDesc("created_date").last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean save(Article entity) {
        return super.save(entity);
    }


//    public List<ArticleDto> listDto() {
//        return getBaseMapper().selectArticleDto();
//    }

    public List<ArticleDto> listDto(QueryWrapper<ArticleDto> queryWrapper) {
        List<ArticleDto> articleDtos = getBaseMapper().selectArticleDto(queryWrapper);
        Set<String> redisViewCountKeys = getRedisViewCountKeys();

        articleDtos.stream().filter(a->redisViewCountKeys.contains(getRedisViewCountKey(a.getId()))).forEach(articleDto -> {
            String id = articleDto.getId();
            Object viewCountObj = redisUtil.get(getRedisViewCountKey(id));
            if (Objects.nonNull(viewCountObj)){
                articleDto.setViewCount((Integer) viewCountObj);
            }
        });
        return articleDtos;
    }


    public ArticleDto getDtoById(String id) {
        ArticleDto articleDto = getBaseMapper().selectArticleDtoById(id);
        updateViewCount(articleDto);
        return articleDto;

    }

    private void updateViewCount(ArticleDto articleDto) {

        String redisViewCountKey = getRedisViewCountKey(articleDto.getId());
        if (!redisUtil.hasKey(redisViewCountKey)) {
            redisUtil.set(redisViewCountKey, articleDto.getViewCount(),120, TimeUnit.SECONDS);
        }
        redisUtil.incr(redisViewCountKey, 1);

    }

    private String getRedisViewCountKey(String id) {
        return REDIS_VIEW_COUNT_PREFIX + id;
    }


    //every 60s
    @Scheduled(fixedRate = 60000)
    public void syncViewCountToDb() {
        Set<String> keys = getRedisViewCountKeys();
        log.info("gonna save view count article keys:{}",keys);
        for (String key : keys) {
            String articleId = key.replace(REDIS_VIEW_COUNT_PREFIX, "");
            Object newViewCount = redisUtil.get(key);
            UpdateWrapper<Article> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", articleId);
            updateWrapper.set("view_count", newViewCount);
            update(updateWrapper);
        }
    }

    private Set<String> getRedisViewCountKeys() {
        return redisUtil.keys(REDIS_VIEW_COUNT_PREFIX + "*");
    }

}

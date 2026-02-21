package com.shiroTest.function.article.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shiroTest.common.Result;
import com.shiroTest.function.article.model.Article;
import com.shiroTest.function.article.model.ArticleDto;
import com.shiroTest.function.article.model.ArticleReadDetailResponse;
import com.shiroTest.function.article.service.IArticleReadRecordService;
import com.shiroTest.function.article.service.impl.ArticleServiceImpl;
import com.shiroTest.function.base.BaseController;
import com.shiroTest.function.user.model.User4Display;
import com.shiroTest.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/article")
public class ArticleController extends BaseController<Article, ArticleServiceImpl> {

    private static final String ID_PATTERN = "^[a-zA-Z0-9-]{1,64}$";

    @Autowired
    private IArticleReadRecordService articleReadRecordService;

    public ArticleController() {
        super(Article.class);
    }

    @GetMapping("/read/latest")
    public Result getLatestArticle() {
        if (getCurrentUserId() == null) {
            return Result.success(getService().getLatestPublicArticle());
        }
        return Result.success(getService().getLatestArticle());
    }

    @Override
    public Result getAll(@RequestParam(defaultValue = "1") int currentPage,
                         @RequestParam(defaultValue = "10") int pageSize,
                         @RequestParam(required = false) String sortBy,
                         @RequestParam(required = false) Boolean ascending,
                         @RequestParam(required = false) String filtersStr) throws IOException {
        return getAllInternal(currentPage, pageSize, filtersStr, true);
    }

    @GetMapping("/manage/findAll")
    public Result getAllForManage(@RequestParam(defaultValue = "1") int currentPage,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  @RequestParam(required = false) String filtersStr) throws IOException {
        return getAllInternal(currentPage, pageSize, filtersStr, false);
    }

    private Result getAllInternal(int currentPage, int pageSize, String filtersStr, boolean hidePrivateForAnonymous) throws IOException {
        PageHelper.startPage(currentPage, pageSize, "created_date DESC");
        QueryWrapper<ArticleDto> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(filtersStr)) {
            Map<String, Object> filters = JsonUtil.toMap(filtersStr);
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue().toString();
                if ("content".equals(key)) {
                    queryWrapper.apply("MATCH(content) AGAINST({0} IN NATURAL LANGUAGE MODE)", val);
                } else {
                    queryWrapper.eq("a." + key, val);
                }
            }
        }
        if (hidePrivateForAnonymous && getCurrentUserId() == null) {
            queryWrapper.eq("a.is_public", 1);
        }
        List<ArticleDto> list = getService().listDto(queryWrapper);
        PageInfo<ArticleDto> pageInfo = new PageInfo<>(list);
        return Result.success(pageInfo);
    }

    @Override
    public Result getById(@PathVariable("id") String id) {
        if (StringUtils.isBlank(id)) {
            return Result.fail("article id cannot be blank");
        }
        if (!id.matches(ID_PATTERN)) {
            return Result.fail("article id format invalid");
        }
        ArticleDto byId = getService().getDtoById(id);
        if (byId != null && Boolean.FALSE.equals(byId.getIsPublic()) && getCurrentUserId() == null) {
            return Result.fail("article is not public");
        }

        HttpServletRequest request = getCurrentRequest();
        if (byId != null) {
            articleReadRecordService.recordRead(
                    id,
                    request == null ? "unknown" : extractClientIp(request),
                    getCurrentUserId(),
                    request == null ? null : request.getHeader("User-Agent")
            );
        }
        return Result.success(byId);
    }

    @GetMapping("/manage/find/{id}")
    public Result getByIdForManage(@PathVariable("id") String id) {
        if (StringUtils.isBlank(id) || !id.matches(ID_PATTERN)) {
            return Result.fail("article id format invalid");
        }
        return Result.success(getService().getDtoById(id));
    }

    @PatchMapping("/manage/public/{id}")
    public Result updatePublicState(@PathVariable("id") String id, @RequestParam("isPublic") Boolean isPublic) {
        if (StringUtils.isBlank(id) || !id.matches(ID_PATTERN)) {
            return Result.fail("article id format invalid");
        }
        if (isPublic == null) {
            return Result.fail("isPublic cannot be null");
        }
        UpdateWrapper<Article> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).set("is_public", isPublic);
        return Result.success(getService().update(updateWrapper));
    }

    @GetMapping("/read/detail/{id}")
    public Result getReadDetail(@PathVariable("id") String id,
                                @RequestParam(defaultValue = "100") int recordLimit,
                                @RequestParam(defaultValue = "30") int dayLimit) {
        if (StringUtils.isBlank(id) || !id.matches(ID_PATTERN)) {
            return Result.fail("article id format invalid");
        }
        Article article = getService().getById(id);
        if (article == null) {
            return Result.fail("article not found");
        }
        ArticleReadDetailResponse detail = articleReadRecordService.getReadDetail(id, recordLimit, dayLimit);
        return Result.success(detail);
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String getCurrentUserId() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal instanceof User4Display) {
                return ((User4Display) principal).getId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}

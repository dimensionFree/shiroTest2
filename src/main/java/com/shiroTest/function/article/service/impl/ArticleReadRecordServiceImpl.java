package com.shiroTest.function.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shiroTest.function.article.model.ArticleReadDetailResponse;
import com.shiroTest.function.article.dao.ArticleReadRecordMapper;
import com.shiroTest.function.article.model.ArticleReadRecord;
import com.shiroTest.function.article.service.IArticleReadRecordService;
import com.shiroTest.function.assistant.service.IRecordIgnoreIpService;
import com.shiroTest.function.assistant.model.GeoContext;
import com.shiroTest.function.assistant.service.AssistantRemoteClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class ArticleReadRecordServiceImpl extends ServiceImpl<ArticleReadRecordMapper, ArticleReadRecord> implements IArticleReadRecordService {

    private static final String UNKNOWN_LOCATION = "UNKNOWN";
    private static final String PRIVATE_NETWORK_LOCATION = "PRIVATE_NETWORK";
    private static final long IP_LOCATION_CACHE_TTL_MILLIS = TimeUnit.HOURS.toMillis(24);

    @Autowired(required = false)
    private AssistantRemoteClient assistantRemoteClient;
    @Autowired
    private IRecordIgnoreIpService recordIgnoreIpService;

    private final ConcurrentHashMap<String, CacheItem<IpGeoSnapshot>> ipLocationCache = new ConcurrentHashMap<>();

    @Override
    public void recordRead(String articleId, String readerIp, String readerUserId, String readerUserAgent) {
        if (StringUtils.isBlank(articleId)) {
            throw new IllegalArgumentException("articleId cannot be blank");
        }
        if (StringUtils.isBlank(readerIp)) {
            throw new IllegalArgumentException("readerIp cannot be blank");
        }
        if (recordIgnoreIpService.shouldIgnore(readerIp)) {
            return;
        }

        ArticleReadRecord record = new ArticleReadRecord();
        record.setArticleId(articleId);
        record.setReaderIp(readerIp);
        IpGeoSnapshot ipGeoSnapshot = resolveReaderIpLocation(readerIp);
        record.setReaderIpLocation(ipGeoSnapshot.locationDisplay);
        record.setReaderIpCountry(ipGeoSnapshot.country);
        record.setReaderIpProvince(ipGeoSnapshot.province);
        record.setReaderIpCity(ipGeoSnapshot.city);
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

    @Override
    public PageInfo<ArticleReadRecord> getManageRecords(int currentPage,
                                                        int pageSize,
                                                        String articleId,
                                                        LocalDate startDate,
                                                        LocalDate endDate) {
        int safeCurrentPage = Math.max(1, currentPage);
        int safePageSize = Math.max(1, Math.min(pageSize, 200));
        try {
            PageHelper.startPage(safeCurrentPage, safePageSize, "read_time DESC,id DESC");
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ArticleReadRecord> queryWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            if (StringUtils.isNotBlank(articleId)) {
                queryWrapper.eq("article_id", articleId.trim());
            }
            if (startDate != null) {
                queryWrapper.ge("read_time", startDate.atStartOfDay());
            }
            if (endDate != null) {
                queryWrapper.lt("read_time", endDate.plusDays(1).atStartOfDay());
            }
            queryWrapper.orderByDesc("read_time").orderByDesc("id");
            List<ArticleReadRecord> records = list(queryWrapper);
            return new PageInfo<>(records);
        } finally {
            PageHelper.clearPage();
        }
    }

    private IpGeoSnapshot resolveReaderIpLocation(String readerIp) {
        String normalizedIp = StringUtils.trimToEmpty(readerIp);
        if (StringUtils.isBlank(normalizedIp)) {
            return IpGeoSnapshot.unknown();
        }
        if (isPrivateOrLocalIp(normalizedIp)) {
            return IpGeoSnapshot.privateNetwork();
        }

        long now = System.currentTimeMillis();
        CacheItem<IpGeoSnapshot> cached = ipLocationCache.get(normalizedIp);
        if (cached != null && cached.expireAt > now && cached.value != null) {
            return cached.value;
        }

        IpGeoSnapshot resolved = IpGeoSnapshot.unknown();
        if (assistantRemoteClient != null) {
            try {
                GeoContext geoContext = assistantRemoteClient.fetchGeoContext(normalizedIp);
                if (geoContext != null) {
                    String country = sanitizeLocationValue(geoContext.getCountry());
                    String province = sanitizeLocationValue(geoContext.getProvince());
                    String city = sanitizeLocationValue(geoContext.getCity());
                    resolved = new IpGeoSnapshot(
                            country,
                            province,
                            city,
                            firstNonBlank(city, province, country, UNKNOWN_LOCATION)
                    );
                }
            } catch (Exception ignored) {
                resolved = IpGeoSnapshot.unknown();
            }
        }
        ipLocationCache.put(normalizedIp, new CacheItem<>(resolved, now + IP_LOCATION_CACHE_TTL_MILLIS));
        return resolved;
    }

    private String sanitizeLocationValue(String value) {
        String trimmed = StringUtils.trimToEmpty(value);
        return StringUtils.isBlank(trimmed) ? UNKNOWN_LOCATION : trimmed;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value) && !UNKNOWN_LOCATION.equals(value)) {
                return value;
            }
        }
        return UNKNOWN_LOCATION;
    }

    private boolean isPrivateOrLocalIp(String ip) {
        if ("localhost".equalsIgnoreCase(ip) || "::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return true;
        }
        if (ip.startsWith("fc") || ip.startsWith("fd") || ip.startsWith("fe80:")) {
            return true;
        }
        String[] segments = ip.split("\\.");
        if (segments.length != 4) {
            return false;
        }
        try {
            int first = Integer.parseInt(segments[0]);
            int second = Integer.parseInt(segments[1]);
            if (first == 10 || first == 127) {
                return true;
            }
            if (first == 192 && second == 168) {
                return true;
            }
            return first == 172 && second >= 16 && second <= 31;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static class CacheItem<T> {
        private final T value;
        private final long expireAt;

        private CacheItem(T value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }
    }

    private static class IpGeoSnapshot {
        private final String country;
        private final String province;
        private final String city;
        private final String locationDisplay;

        private IpGeoSnapshot(String country, String province, String city, String locationDisplay) {
            this.country = country;
            this.province = province;
            this.city = city;
            this.locationDisplay = locationDisplay;
        }

        private static IpGeoSnapshot privateNetwork() {
            return new IpGeoSnapshot(
                    PRIVATE_NETWORK_LOCATION,
                    PRIVATE_NETWORK_LOCATION,
                    PRIVATE_NETWORK_LOCATION,
                    PRIVATE_NETWORK_LOCATION
            );
        }

        private static IpGeoSnapshot unknown() {
            return new IpGeoSnapshot(
                    UNKNOWN_LOCATION,
                    UNKNOWN_LOCATION,
                    UNKNOWN_LOCATION,
                    UNKNOWN_LOCATION
            );
        }
    }
}

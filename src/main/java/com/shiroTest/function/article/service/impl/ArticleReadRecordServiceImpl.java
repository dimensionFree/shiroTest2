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
import com.shiroTest.utils.JsonUtil;
import com.shiroTest.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class ArticleReadRecordServiceImpl extends ServiceImpl<ArticleReadRecordMapper, ArticleReadRecord> implements IArticleReadRecordService {

    private static final String UNKNOWN_LOCATION = "UNKNOWN";
    private static final String PRIVATE_NETWORK_LOCATION = "PRIVATE_NETWORK";
    private static final long IP_LOCATION_CACHE_TTL_MILLIS = TimeUnit.HOURS.toMillis(24);
    private static final String READ_CACHE_KEY_PREFIX = "ARTICLE_READ_AGG_";
    private static final DateTimeFormatter READ_BUCKET_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final long READ_CACHE_KEY_TTL_SECONDS = 180L;
    private static final int DB_TIMEZONE_OFFSET_HOURS = 9;

    @Autowired(required = false)
    private AssistantRemoteClient assistantRemoteClient;
    @Autowired
    private IRecordIgnoreIpService recordIgnoreIpService;
    @Autowired
    private RedisUtil redisUtil;

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
        record.setReadTime(nowUtc());
        cacheReadRecord(record);
    }

    @Scheduled(fixedRate = 60000)
    public void flushCachedReadRecordsToDb() {
        flushCachedReadRecords(false);
    }

    @Override
    public int flushAllCachedReadRecordsToDb() {
        return flushCachedReadRecords(true);
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

    private int flushCachedReadRecords(boolean includeCurrentBucket) {
        Set<String> keys = redisUtil.keys(READ_CACHE_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        String currentBucket = currentMinuteBucket();
        int totalPersistedCount = 0;
        for (String key : keys) {
            String bucket = bucketFromCacheKey(key);
            if (StringUtils.isBlank(bucket)) {
                continue;
            }
            if (!includeCurrentBucket && bucket.compareTo(currentBucket) >= 0) {
                continue;
            }
            totalPersistedCount += flushOneReadCacheKey(key);
        }
        return totalPersistedCount;
    }

    private int flushOneReadCacheKey(String key) {
        Map<Object, Object> cachedMap = redisUtil.hGetAll(key);
        if (cachedMap == null || cachedMap.isEmpty()) {
            redisUtil.delete(key);
            return 0;
        }
        ArrayList<ArticleReadRecord> records = new ArrayList<>();
        for (Object value : cachedMap.values()) {
            if (value == null) {
                continue;
            }
            Map<String, Object> cacheItem;
            try {
                cacheItem = JsonUtil.toMap(value.toString());
            } catch (Exception ignored) {
                continue;
            }
            String articleId = trimToNull(cacheItem.get("articleId"));
            String readerIp = trimToNull(cacheItem.get("readerIp"));
            if (StringUtils.isBlank(articleId) || StringUtils.isBlank(readerIp)) {
                continue;
            }
            ArticleReadRecord record = new ArticleReadRecord();
            record.setArticleId(articleId);
            record.setReaderIp(readerIp);
            record.setReaderIpLocation(trimToNull(cacheItem.get("readerIpLocation")));
            record.setReaderIpCountry(trimToNull(cacheItem.get("readerIpCountry")));
            record.setReaderIpProvince(trimToNull(cacheItem.get("readerIpProvince")));
            record.setReaderIpCity(trimToNull(cacheItem.get("readerIpCity")));
            record.setReaderUserId(trimToNull(cacheItem.get("readerUserId")));
            record.setReaderUserAgent(trimToNull(cacheItem.get("readerUserAgent")));
            String readTime = trimToNull(cacheItem.get("readTime"));
            LocalDateTime sourceReadTime = StringUtils.isBlank(readTime) ? nowUtc() : LocalDateTime.parse(readTime);
            // 入库前统一按 UTC+9 修正，避免服务器时区差异导致展示时间偏移。
            record.setReadTime(adjustToDbTimezone(sourceReadTime));
            records.add(record);
        }
        int insertedCount = 0;
        for (ArticleReadRecord record : records) {
            try {
                insertedCount += getBaseMapper().insert(record);
            } catch (Exception ignored) {
                // 缓存回放过程中遇到脏数据（如被删除文章的旧记录）时跳过，避免影响其余记录入库。
            }
        }
        redisUtil.delete(key);
        return insertedCount;
    }

    private void cacheReadRecord(ArticleReadRecord record) {
        String key = READ_CACHE_KEY_PREFIX + currentMinuteBucket();
        String field = buildReadField(record);
        Map<String, Object> cacheItem = new HashMap<>();
        cacheItem.put("articleId", record.getArticleId());
        cacheItem.put("readerIp", record.getReaderIp());
        cacheItem.put("readerIpLocation", record.getReaderIpLocation());
        cacheItem.put("readerIpCountry", record.getReaderIpCountry());
        cacheItem.put("readerIpProvince", record.getReaderIpProvince());
        cacheItem.put("readerIpCity", record.getReaderIpCity());
        cacheItem.put("readerUserId", record.getReaderUserId());
        cacheItem.put("readerUserAgent", record.getReaderUserAgent());
        cacheItem.put("readTime", record.getReadTime() == null ? null : record.getReadTime().toString());
        redisUtil.hPut(key, field, JsonUtil.toJson(cacheItem));
        redisUtil.expire(key, READ_CACHE_KEY_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private String buildReadField(ArticleReadRecord record) {
        return String.format(
                "%s|%s|%s|%s",
                StringUtils.trimToEmpty(record.getArticleId()),
                StringUtils.trimToEmpty(record.getReaderIp()),
                StringUtils.trimToEmpty(record.getReadTime() == null ? null : record.getReadTime().toString()),
                UUID.randomUUID()
        );
    }

    private String currentMinuteBucket() {
        return nowUtc().format(READ_BUCKET_FORMAT);
    }

    private String bucketFromCacheKey(String key) {
        if (!StringUtils.startsWith(key, READ_CACHE_KEY_PREFIX)) {
            return null;
        }
        return key.substring(READ_CACHE_KEY_PREFIX.length());
    }

    private String trimToNull(Object value) {
        return value == null ? null : StringUtils.trimToNull(value.toString());
    }

    private LocalDateTime adjustToDbTimezone(LocalDateTime sourceTime) {
        if (sourceTime == null) {
            return null;
        }
        return sourceTime.plusHours(DB_TIMEZONE_OFFSET_HOURS);
    }

    private LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
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

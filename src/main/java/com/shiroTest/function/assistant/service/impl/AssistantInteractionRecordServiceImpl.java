package com.shiroTest.function.assistant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shiroTest.function.assistant.dao.AssistantInteractionRecordMapper;
import com.shiroTest.function.assistant.model.AssistantInteractionRecord;
import com.shiroTest.function.assistant.model.GeoContext;
import com.shiroTest.function.assistant.service.AssistantRemoteClient;
import com.shiroTest.function.assistant.service.IAssistantInteractionRecordService;
import com.shiroTest.function.assistant.service.IRecordIgnoreIpService;
import com.shiroTest.utils.JsonUtil;
import com.shiroTest.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class AssistantInteractionRecordServiceImpl extends ServiceImpl<AssistantInteractionRecordMapper, AssistantInteractionRecord>
        implements IAssistantInteractionRecordService {

    private static final String UNKNOWN_LOCATION = "UNKNOWN";
    private static final String PRIVATE_NETWORK_LOCATION = "PRIVATE_NETWORK";
    private static final int PAYLOAD_MAX_LENGTH = 4000;
    private static final long IP_LOCATION_CACHE_TTL_MILLIS = TimeUnit.HOURS.toMillis(24);
    private static final String INTERACTION_CACHE_KEY_PREFIX = "ASSISTANT_INTERACTION_AGG_";
    private static final DateTimeFormatter INTERACTION_BUCKET_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final long INTERACTION_CACHE_KEY_TTL_SECONDS = 180L;
    private static final String INTERACTION_TYPE_CHAT = "CHAT";

    @Autowired(required = false)
    private AssistantRemoteClient assistantRemoteClient;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IRecordIgnoreIpService recordIgnoreIpService;

    private final ConcurrentHashMap<String, CacheItem<IpGeoSnapshot>> ipLocationCache = new ConcurrentHashMap<>();

    @Override
    public void recordInteraction(String interactionType,
                                  String interactionAction,
                                  Map<String, Object> interactionPayload,
                                  String clientIp,
                                  String userId,
                                  String userAgent) {
        if (StringUtils.isBlank(interactionType)) {
            throw new IllegalArgumentException("interactionType cannot be blank");
        }
        if (StringUtils.isBlank(interactionAction)) {
            throw new IllegalArgumentException("interactionAction cannot be blank");
        }
        if (StringUtils.isBlank(clientIp)) {
            throw new IllegalArgumentException("clientIp cannot be blank");
        }
        if (recordIgnoreIpService.shouldIgnore(clientIp)) {
            return;
        }

        IpGeoSnapshot ipGeoSnapshot = resolveClientIpLocation(clientIp);
        AssistantInteractionRecord record = new AssistantInteractionRecord();
        record.setInteractionType(interactionType.trim());
        record.setInteractionAction(interactionAction.trim());
        record.setInteractionPayload(toPayloadJson(interactionPayload));
        record.setClientIp(clientIp.trim());
        record.setClientIpLocation(ipGeoSnapshot.locationDisplay);
        record.setClientIpCountry(ipGeoSnapshot.country);
        record.setClientIpProvince(ipGeoSnapshot.province);
        record.setClientIpCity(ipGeoSnapshot.city);
        record.setUserId(StringUtils.trimToNull(userId));
        record.setUserAgent(StringUtils.trimToNull(userAgent));
        record.setTriggerTime(LocalDateTime.now());

        if (shouldBypassAggregation(record.getInteractionType())) {
            save(record);
            return;
        }
        cacheInteractionRecord(record);
    }

    @Scheduled(fixedRate = 60000)
    public void flushAggregatedInteractionsToDb() {
        flushCachedInteractions(false);
    }

    @Override
    public int flushAllCachedInteractionsToDb() {
        return flushCachedInteractions(true);
    }

    private int flushCachedInteractions(boolean includeCurrentBucket) {
        Set<String> keys = redisUtil.keys(INTERACTION_CACHE_KEY_PREFIX + "*");
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
            totalPersistedCount += flushOneCacheKey(key);
        }
        return totalPersistedCount;
    }

    @Override
    public PageInfo<AssistantInteractionRecord> getManageRecords(int currentPage,
                                                                 int pageSize,
                                                                 String interactionType,
                                                                 String interactionAction,
                                                                 LocalDate startDate,
                                                                 LocalDate endDate) {
        int safeCurrentPage = Math.max(1, currentPage);
        int safePageSize = Math.max(1, Math.min(pageSize, 200));
        try {
            PageHelper.startPage(safeCurrentPage, safePageSize, "trigger_time DESC,id DESC");
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AssistantInteractionRecord> queryWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            if (StringUtils.isNotBlank(interactionType)) {
                queryWrapper.eq("interaction_type", interactionType.trim());
            }
            if (StringUtils.isNotBlank(interactionAction)) {
                queryWrapper.eq("interaction_action", interactionAction.trim());
            }
            if (startDate != null) {
                queryWrapper.ge("trigger_time", startDate.atStartOfDay());
            }
            if (endDate != null) {
                queryWrapper.lt("trigger_time", endDate.plusDays(1).atStartOfDay());
            }
            queryWrapper.orderByDesc("trigger_time").orderByDesc("id");
            List<AssistantInteractionRecord> records = list(queryWrapper);
            return new PageInfo<>(records);
        } finally {
            PageHelper.clearPage();
        }
    }

    private int flushOneCacheKey(String key) {
        Map<Object, Object> cachedMap = redisUtil.hGetAll(key);
        if (cachedMap == null || cachedMap.isEmpty()) {
            redisUtil.delete(key);
            return 0;
        }
        ArrayList<AssistantInteractionRecord> records = new ArrayList<>();
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
            String interactionType = trimToNull(cacheItem.get("interactionType"));
            String interactionAction = trimToNull(cacheItem.get("interactionAction"));
            if (StringUtils.isBlank(interactionType) || StringUtils.isBlank(interactionAction)) {
                continue;
            }
            AssistantInteractionRecord record = new AssistantInteractionRecord();
            record.setInteractionType(interactionType);
            record.setInteractionAction(interactionAction);
            record.setInteractionPayload(trimToNull(cacheItem.get("interactionPayload")));
            record.setClientIp(trimToNull(cacheItem.get("clientIp")));
            record.setClientIpLocation(trimToNull(cacheItem.get("clientIpLocation")));
            record.setClientIpCountry(trimToNull(cacheItem.get("clientIpCountry")));
            record.setClientIpProvince(trimToNull(cacheItem.get("clientIpProvince")));
            record.setClientIpCity(trimToNull(cacheItem.get("clientIpCity")));
            record.setUserId(trimToNull(cacheItem.get("userId")));
            record.setUserAgent(trimToNull(cacheItem.get("userAgent")));
            String triggerTime = trimToNull(cacheItem.get("triggerTime"));
            record.setTriggerTime(StringUtils.isBlank(triggerTime) ? LocalDateTime.now() : LocalDateTime.parse(triggerTime));
            records.add(record);
        }
        if (!records.isEmpty()) {
            saveBatch(records, 100);
        }
        redisUtil.delete(key);
        return records.size();
    }

    private boolean shouldBypassAggregation(String interactionType) {
        // 未来 AI 聊天消息需要完整追踪，不走“每分钟按 IP+类型+动作合并”策略。
        return INTERACTION_TYPE_CHAT.equalsIgnoreCase(StringUtils.trimToEmpty(interactionType));
    }

    private void cacheInteractionRecord(AssistantInteractionRecord record) {
        String key = INTERACTION_CACHE_KEY_PREFIX + currentMinuteBucket();
        String field = buildInteractionField(record);
        Map<String, Object> cacheItem = new HashMap<>();
        cacheItem.put("interactionType", record.getInteractionType());
        cacheItem.put("interactionAction", record.getInteractionAction());
        cacheItem.put("interactionPayload", record.getInteractionPayload());
        cacheItem.put("clientIp", record.getClientIp());
        cacheItem.put("clientIpLocation", record.getClientIpLocation());
        cacheItem.put("clientIpCountry", record.getClientIpCountry());
        cacheItem.put("clientIpProvince", record.getClientIpProvince());
        cacheItem.put("clientIpCity", record.getClientIpCity());
        cacheItem.put("userId", record.getUserId());
        cacheItem.put("userAgent", record.getUserAgent());
        cacheItem.put("triggerTime", record.getTriggerTime() == null ? null : record.getTriggerTime().toString());
        redisUtil.hPut(key, field, JsonUtil.toJson(cacheItem));
        redisUtil.expire(key, INTERACTION_CACHE_KEY_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private String buildInteractionField(AssistantInteractionRecord record) {
        return String.format(
                "%s|%s|%s",
                StringUtils.trimToEmpty(record.getClientIp()),
                StringUtils.trimToEmpty(record.getInteractionType()),
                StringUtils.trimToEmpty(record.getInteractionAction())
        );
    }

    private String currentMinuteBucket() {
        return LocalDateTime.now().format(INTERACTION_BUCKET_FORMAT);
    }

    private String bucketFromCacheKey(String key) {
        if (!StringUtils.startsWith(key, INTERACTION_CACHE_KEY_PREFIX)) {
            return null;
        }
        return key.substring(INTERACTION_CACHE_KEY_PREFIX.length());
    }

    private String trimToNull(Object value) {
        return value == null ? null : StringUtils.trimToNull(value.toString());
    }

    private String toPayloadJson(Map<String, Object> interactionPayload) {
        if (interactionPayload == null || interactionPayload.isEmpty()) {
            return null;
        }
        String json = JsonUtil.toJson(interactionPayload);
        if (json.length() <= PAYLOAD_MAX_LENGTH) {
            return json;
        }
        return json.substring(0, PAYLOAD_MAX_LENGTH);
    }

    private IpGeoSnapshot resolveClientIpLocation(String clientIp) {
        String normalizedIp = StringUtils.trimToEmpty(clientIp);
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


package com.shiroTest.function.assistant.service.impl;

import com.shiroTest.function.assistant.model.AssistantContextResponse;
import com.shiroTest.function.assistant.model.GeoContext;
import com.shiroTest.function.assistant.model.WeatherContext;
import com.shiroTest.function.assistant.service.AssistantRemoteClient;
import com.shiroTest.function.assistant.service.IAssistantService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class AssistantServiceImpl implements IAssistantService {

    private static final long GEO_TTL_MILLIS = TimeUnit.HOURS.toMillis(24);
    private static final long WEATHER_TTL_MILLIS = TimeUnit.MINUTES.toMillis(20);
    private static final String UNKNOWN_IP_KEY = "UNKNOWN_IP";

    private final AssistantRemoteClient assistantRemoteClient;
    @Value("${app.dev.use-public-ip-for-local:false}")
    private boolean useDevPublicIpForLocal;
    @Value("${app.dev.public-ip:}")
    private String devPublicIp;

    private final ConcurrentHashMap<String, CacheItem<GeoContext>> geoCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CacheItem<WeatherContext>> weatherCache = new ConcurrentHashMap<>();

    public AssistantServiceImpl(AssistantRemoteClient assistantRemoteClient) {
        this.assistantRemoteClient = assistantRemoteClient;
    }

    @Override
    public AssistantContextResponse getContextByIp(String clientIp) {
        GeoContext geoContext = getGeoContextWithCache(clientIp);
        WeatherContext weatherContext = getWeatherContextWithCache(geoContext.getLatitude(), geoContext.getLongitude());

        return new AssistantContextResponse(
                geoContext.getCity(),
                geoContext.getLatitude(),
                geoContext.getLongitude(),
                weatherContext.getWeatherCode(),
                weatherContext.getTemperature()
        );
    }

    private GeoContext getGeoContextWithCache(String clientIp) {
        long now = System.currentTimeMillis();
        String normalizedIp = StringUtils.trimToNull(clientIp);
        String ipForGeoLookup = resolveIpForGeoLookup(normalizedIp);
        String cacheKey = StringUtils.isBlank(ipForGeoLookup) ? UNKNOWN_IP_KEY : ipForGeoLookup;
        CacheItem<GeoContext> cached = geoCache.get(cacheKey);
        if (cached != null && cached.expireAt > now && cached.value != null) {
            return cached.value;
        }
        GeoContext fetched = assistantRemoteClient.fetchGeoContext(ipForGeoLookup);
        GeoContext safeValue = fetched == null ? new GeoContext("", "", "", null, null) : fetched;
        geoCache.put(cacheKey, new CacheItem<>(safeValue, now + GEO_TTL_MILLIS));
        return safeValue;
    }

    private String resolveIpForGeoLookup(String originalIp) {
        if (StringUtils.isBlank(originalIp) || !isPrivateOrLocalIp(originalIp)) {
            return originalIp;
        }
        if (!useDevPublicIpForLocal) {
            return originalIp;
        }
        String configuredIp = StringUtils.trimToNull(devPublicIp);
        if (configuredIp == null || isPrivateOrLocalIp(configuredIp)) {
            return originalIp;
        }
        // 为什么：本地联调时优先使用配置的公网 IP，避免 localhost/内网地址无法解析城市。
        return configuredIp;
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

    private WeatherContext getWeatherContextWithCache(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return new WeatherContext(null, null);
        }
        long now = System.currentTimeMillis();
        String cacheKey = buildWeatherCacheKey(latitude, longitude);
        CacheItem<WeatherContext> cached = weatherCache.get(cacheKey);
        if (cached != null && cached.expireAt > now && cached.value != null) {
            return cached.value;
        }
        WeatherContext fetched = assistantRemoteClient.fetchWeatherContext(latitude, longitude);
        WeatherContext safeValue = fetched == null ? new WeatherContext(null, null) : fetched;
        weatherCache.put(cacheKey, new CacheItem<>(safeValue, now + WEATHER_TTL_MILLIS));
        return safeValue;
    }

    private String buildWeatherCacheKey(Double latitude, Double longitude) {
        // 为什么取两位小数：降低天气缓存碎片，避免同城因定位抖动重复请求第三方接口。
        return String.format(Locale.ROOT, "%.2f,%.2f", latitude, longitude);
    }

    private static class CacheItem<T> {
        private final T value;
        private final long expireAt;

        private CacheItem(T value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }
    }
}

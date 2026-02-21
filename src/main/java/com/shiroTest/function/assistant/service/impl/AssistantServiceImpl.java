package com.shiroTest.function.assistant.service.impl;

import com.shiroTest.function.assistant.model.AssistantContextResponse;
import com.shiroTest.function.assistant.model.GeoContext;
import com.shiroTest.function.assistant.model.WeatherContext;
import com.shiroTest.function.assistant.service.AssistantRemoteClient;
import com.shiroTest.function.assistant.service.IAssistantService;
import org.apache.commons.lang3.StringUtils;
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
        String cacheKey = StringUtils.isBlank(clientIp) ? UNKNOWN_IP_KEY : clientIp;
        CacheItem<GeoContext> cached = geoCache.get(cacheKey);
        if (cached != null && cached.expireAt > now && cached.value != null) {
            return cached.value;
        }
        GeoContext fetched = assistantRemoteClient.fetchGeoContext(clientIp);
        GeoContext safeValue = fetched == null ? new GeoContext("", null, null) : fetched;
        geoCache.put(cacheKey, new CacheItem<>(safeValue, now + GEO_TTL_MILLIS));
        return safeValue;
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

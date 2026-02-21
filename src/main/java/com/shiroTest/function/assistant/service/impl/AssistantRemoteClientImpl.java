package com.shiroTest.function.assistant.service.impl;

import cn.hutool.http.HttpUtil;
import com.shiroTest.function.assistant.model.GeoContext;
import com.shiroTest.function.assistant.model.WeatherContext;
import com.shiroTest.function.assistant.service.AssistantRemoteClient;
import com.shiroTest.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AssistantRemoteClientImpl implements AssistantRemoteClient {

    private static final String IPAPI_URL = "https://ipapi.co/json/";
    private static final String IPWHO_URL = "https://ipwho.is/";
    private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast";
    private static final int TIMEOUT_MS = 3000;

    @Override
    public GeoContext fetchGeoContext(String clientIp) {
        GeoContext fromIpApi = fetchFromIpApi(clientIp);
        if (isGeoAvailable(fromIpApi)) {
            return fromIpApi;
        }
        GeoContext fromIpWho = fetchFromIpWho(clientIp);
        if (isGeoAvailable(fromIpWho)) {
            return fromIpWho;
        }
        return new GeoContext("", null, null);
    }

    @Override
    public WeatherContext fetchWeatherContext(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return new WeatherContext(null, null);
        }
        try {
            String url = OPEN_METEO_URL
                    + "?latitude=" + latitude
                    + "&longitude=" + longitude
                    + "&current=temperature_2m,weather_code&timezone=auto";
            String body = HttpUtil.get(url, TIMEOUT_MS);
            Map<String, Object> data = JsonUtil.toMap(body);
            Map<String, Object> current = asMap(data.get("current"));
            if (current == null) {
                return new WeatherContext(null, null);
            }
            Integer weatherCode = toInteger(current.get("weather_code"));
            Double temperature = toDouble(current.get("temperature_2m"));
            return new WeatherContext(weatherCode, temperature);
        } catch (Exception ignored) {
            return new WeatherContext(null, null);
        }
    }

    private GeoContext fetchFromIpApi(String clientIp) {
        try {
            String url = StringUtils.isBlank(clientIp) ? IPAPI_URL : "https://ipapi.co/" + clientIp + "/json/";
            String body = HttpUtil.get(url, TIMEOUT_MS);
            Map<String, Object> data = JsonUtil.toMap(body);
            String city = toText(data.get("city"));
            Double latitude = toDouble(data.get("latitude"));
            Double longitude = toDouble(data.get("longitude"));
            return new GeoContext(city, latitude, longitude);
        } catch (Exception ignored) {
            return new GeoContext("", null, null);
        }
    }

    private GeoContext fetchFromIpWho(String clientIp) {
        try {
            String url = StringUtils.isBlank(clientIp) ? IPWHO_URL : (IPWHO_URL + clientIp);
            String body = HttpUtil.get(url, TIMEOUT_MS);
            Map<String, Object> data = JsonUtil.toMap(body);
            Object success = data.get("success");
            if (Boolean.FALSE.equals(success)) {
                return new GeoContext("", null, null);
            }
            String city = toText(data.get("city"));
            Double latitude = toDouble(data.get("latitude"));
            Double longitude = toDouble(data.get("longitude"));
            return new GeoContext(city, latitude, longitude);
        } catch (Exception ignored) {
            return new GeoContext("", null, null);
        }
    }

    private boolean isGeoAvailable(GeoContext geoContext) {
        if (geoContext == null) {
            return false;
        }
        return StringUtils.isNotBlank(geoContext.getCity())
                || (geoContext.getLatitude() != null && geoContext.getLongitude() != null);
    }

    private String toText(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Double.valueOf(value.toString());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (Exception ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }
}

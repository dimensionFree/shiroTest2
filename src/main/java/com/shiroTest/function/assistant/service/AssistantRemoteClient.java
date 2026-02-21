package com.shiroTest.function.assistant.service;

import com.shiroTest.function.assistant.model.GeoContext;
import com.shiroTest.function.assistant.model.WeatherContext;

public interface AssistantRemoteClient {
    GeoContext fetchGeoContext(String clientIp);

    WeatherContext fetchWeatherContext(Double latitude, Double longitude);
}

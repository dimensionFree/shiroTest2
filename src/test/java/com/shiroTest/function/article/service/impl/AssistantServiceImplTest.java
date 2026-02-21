package com.shiroTest.function.assistant.service.impl;

import com.shiroTest.function.assistant.model.AssistantContextResponse;
import com.shiroTest.function.assistant.model.GeoContext;
import com.shiroTest.function.assistant.model.WeatherContext;
import com.shiroTest.function.assistant.service.AssistantRemoteClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssistantServiceImplTest {

    @Mock
    private AssistantRemoteClient assistantRemoteClient;

    @InjectMocks
    private AssistantServiceImpl assistantService;

    @Test
    void getContextByIp_should_return_geo_and_weather() {
        // Given
        when(assistantRemoteClient.fetchGeoContext("1.2.3.4"))
                .thenReturn(new GeoContext("Tokyo", 35.68, 139.76));
        when(assistantRemoteClient.fetchWeatherContext(35.68, 139.76))
                .thenReturn(new WeatherContext(0, 26.5));

        // When
        AssistantContextResponse response = assistantService.getContextByIp("1.2.3.4");

        // Then
        assertThat(response.getCity()).isEqualTo("Tokyo");
        assertThat(response.getLatitude()).isEqualTo(35.68);
        assertThat(response.getLongitude()).isEqualTo(139.76);
        assertThat(response.getWeatherCode()).isEqualTo(0);
        assertThat(response.getTemperature()).isEqualTo(26.5);
    }

    @Test
    void getContextByIp_should_hit_geo_cache_for_same_ip() {
        // Given
        when(assistantRemoteClient.fetchGeoContext("8.8.8.8"))
                .thenReturn(new GeoContext("Osaka", 34.69, 135.50));
        when(assistantRemoteClient.fetchWeatherContext(34.69, 135.50))
                .thenReturn(new WeatherContext(3, 20.0));

        // When
        assistantService.getContextByIp("8.8.8.8");
        assistantService.getContextByIp("8.8.8.8");

        // Then
        verify(assistantRemoteClient, times(1)).fetchGeoContext("8.8.8.8");
    }

    @Test
    void getContextByIp_should_hit_weather_cache_for_close_coordinates() {
        // Given
        when(assistantRemoteClient.fetchGeoContext("9.9.9.1"))
                .thenReturn(new GeoContext("Yokohama", 35.6581, 139.7432));
        when(assistantRemoteClient.fetchGeoContext("9.9.9.2"))
                .thenReturn(new GeoContext("Yokohama", 35.6599, 139.7449));
        when(assistantRemoteClient.fetchWeatherContext(35.6581, 139.7432))
                .thenReturn(new WeatherContext(2, 24.2));

        // When
        assistantService.getContextByIp("9.9.9.1");
        assistantService.getContextByIp("9.9.9.2");

        // Then
        verify(assistantRemoteClient, times(1)).fetchWeatherContext(35.6581, 139.7432);
    }
}

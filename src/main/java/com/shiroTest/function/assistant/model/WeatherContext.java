package com.shiroTest.function.assistant.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherContext {
    private Integer weatherCode;
    private Double temperature;
}

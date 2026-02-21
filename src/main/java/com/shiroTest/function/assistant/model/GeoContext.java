package com.shiroTest.function.assistant.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoContext {
    private String city;
    private Double latitude;
    private Double longitude;
}

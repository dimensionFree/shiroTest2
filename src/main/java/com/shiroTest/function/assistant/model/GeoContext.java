package com.shiroTest.function.assistant.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeoContext {
    private String country;
    private String province;
    private String city;
    private Double latitude;
    private Double longitude;

    public GeoContext(String city, Double latitude, Double longitude) {
        this("", "", city, latitude, longitude);
    }

    public GeoContext(String country, String province, String city, Double latitude, Double longitude) {
        this.country = country;
        this.province = province;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

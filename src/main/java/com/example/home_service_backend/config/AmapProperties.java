package com.example.home_service_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "amap")
public record AmapProperties(String apiKey, String webApiKey, String securityKey, String baseUrl, String geocodePath,
                             String regeocodePath, Duration connectTimeout, Duration readTimeout,
                             int maxRetries) {
    public AmapProperties {
        webApiKey = defaultValue(webApiKey, apiKey);
        baseUrl = defaultValue(baseUrl, "https://restapi.amap.com");
        geocodePath = defaultValue(geocodePath, "/v3/geocode/geo");
        regeocodePath = defaultValue(regeocodePath, "/v3/geocode/regeo");
        connectTimeout = valid(connectTimeout, Duration.ofSeconds(3));
        readTimeout = valid(readTimeout, Duration.ofSeconds(5));
        maxRetries = maxRetries <= 0 ? 2 : Math.min(maxRetries, 3);
    }
    private static String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
    private static Duration valid(Duration value, Duration fallback) {
        return value == null || value.isZero() || value.isNegative() ? fallback : value;
    }
}

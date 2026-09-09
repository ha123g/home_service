package com.example.home_service_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "tencent.cos")
public record CosProperties(String secretId, String secretKey, String region, String bucket,
                            Duration connectTimeout, Duration readTimeout, int maxRetries,
                            Duration previewTtl, long maxFileSize) {
    public CosProperties {
        region = defaultValue(region, "ap-beijing");
        connectTimeout = valid(connectTimeout, Duration.ofSeconds(3));
        readTimeout = valid(readTimeout, Duration.ofSeconds(15));
        maxRetries = maxRetries <= 0 ? 2 : Math.min(maxRetries, 3);
        previewTtl = valid(previewTtl, Duration.ofMinutes(15));
        if (previewTtl.compareTo(Duration.ofHours(1)) > 0) {
            previewTtl = Duration.ofHours(1);
        }
        maxFileSize = maxFileSize <= 0 ? 5 * 1024 * 1024L : Math.min(maxFileSize, 10 * 1024 * 1024L);
    }

    public boolean configured() {
        return notBlank(secretId) && notBlank(secretKey) && notBlank(bucket) && notBlank(region);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static Duration valid(Duration value, Duration fallback) {
        return value == null || value.isZero() || value.isNegative() ? fallback : value;
    }
}

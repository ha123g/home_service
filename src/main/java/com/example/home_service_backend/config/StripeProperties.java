package com.example.home_service_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "payment.stripe")
public record StripeProperties(boolean enabled, boolean sandbox, String secretKey, String publishableKey,
                               String checkoutBaseUrl, String apiBaseUrl, long expireMinutes,
                               Duration connectTimeout, Duration readTimeout, int maxRetries) {
    public StripeProperties {
        apiBaseUrl = apiBaseUrl == null || apiBaseUrl.isBlank() ? "https://api.stripe.com" : apiBaseUrl;
        expireMinutes = expireMinutes <= 0 ? 30 : Math.min(expireMinutes, 1440);
        connectTimeout = valid(connectTimeout, Duration.ofSeconds(3));
        readTimeout = valid(readTimeout, Duration.ofSeconds(8));
        maxRetries = maxRetries < 0 ? 0 : Math.min(maxRetries, 2);
    }

    private static Duration valid(Duration value, Duration fallback) {
        return value == null || value.isZero() || value.isNegative() ? fallback : value;
    }
}

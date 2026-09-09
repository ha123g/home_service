package com.example.home_service_backend.dto.request.order;
import jakarta.validation.constraints.Min;
public record PublishOrderRequest(@Min(1) long expireMinutes) {
    public PublishOrderRequest { if (expireMinutes == 0) expireMinutes = 30; }
}

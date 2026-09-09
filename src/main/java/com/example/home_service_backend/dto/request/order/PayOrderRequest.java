package com.example.home_service_backend.dto.request.order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record PayOrderRequest(@NotBlank @Size(max = 128) String idempotencyKey) {}

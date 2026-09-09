package com.example.home_service_backend.dto.request.order;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateOrderRequest(
        @NotNull Long appointmentId,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @Size(max = 64) String workerName,
        Long workerId,
        LocalDateTime scheduledStart,
        LocalDateTime scheduledEnd,
        @Size(max = 512) String remark,
        @Size(max = 128) String idempotencyKey) {}

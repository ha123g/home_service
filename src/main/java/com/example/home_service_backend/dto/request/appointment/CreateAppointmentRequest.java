package com.example.home_service_backend.dto.request.appointment;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CreateAppointmentRequest(
        @NotNull Long shopId,
        @NotNull Long serviceId,
        @NotNull Long addressId,
        @Size(max = 128) String serviceTitleInput,
        @NotBlank @Size(max = 2048) String requirementText,
        @FutureOrPresent LocalDateTime preferredStart,
        LocalDateTime preferredEnd,
        @Size(max = 64) String contactName,
        @Pattern(regexp = "^[0-9+()\\- ]{6,32}$") String contactPhone,
        @Size(max = 128) String idempotencyKey) {}

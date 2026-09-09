package com.example.home_service_backend.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOrderStatusRequest(@NotBlank @Size(max = 24) String status,
                                       @Size(max = 512) String reason) {
}

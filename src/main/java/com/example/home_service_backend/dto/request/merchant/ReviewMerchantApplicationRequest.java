package com.example.home_service_backend.dto.request.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewMerchantApplicationRequest(
        @NotBlank String decision,
        @Size(max = 512) String remark) {}

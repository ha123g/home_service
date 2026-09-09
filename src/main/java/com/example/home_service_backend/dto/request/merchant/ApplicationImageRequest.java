package com.example.home_service_backend.dto.request.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

public record ApplicationImageRequest(
        @NotBlank @Pattern(regexp = "LOGO|QUALIFICATION|ID_FRONT|ID_BACK|OTHER") String imageType,
        @NotBlank @Size(max = 512) String objectKey,
        @Pattern(regexp = "image/jpeg|image/png|image/webp") String mimeType,
        @Positive @Max(10485760) Long fileSize,
        @Pattern(regexp = "^[a-fA-F0-9]{64}$") String sha256) {}

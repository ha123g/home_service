package com.example.home_service_backend.dto.request.merchant;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;

public record CreateMerchantApplicationRequest(
        @NotBlank @Size(max = 64) String realName,
        @NotBlank @Size(max = 32) String phone,
        @NotBlank @Size(max = 128) String shopName,
        @Size(max = 1024) String intro,
        @Size(max = 255) String serviceArea,
        @NotNull @DecimalMin("0.1") @DecimalMax("500") BigDecimal serviceRadiusKm,
        @NotBlank @Size(max = 255) String addressDetail,
        @Size(max = 64) String province,
        @NotBlank @Size(max = 64) String city,
        @Size(max = 64) String district,
        @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
        @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
        @Size(max = 512) String logoObjectKey,
        @Valid @Size(max = 20) List<ApplicationImageRequest> images,
        @Size(max = 30) List<Long> serviceIds,
        @Size(max = 30) List<Long> categoryIds,
        @Valid @Size(max = 30) List<MerchantServiceItemRequest> serviceItems,
        @Size(max = 12) List<@NotBlank @Size(max = 32) String> tags) {}

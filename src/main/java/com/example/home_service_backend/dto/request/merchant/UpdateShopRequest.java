package com.example.home_service_backend.dto.request.merchant;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
public record UpdateShopRequest(@Size(max=128) String shopName,@Size(max=512) String shopLogoObjectKey,@Size(max=2048) String shopIntro,@Size(max=32) String contactPhone,@Size(max=255) String serviceArea,@DecimalMin("0.1") @DecimalMax("500") BigDecimal serviceRadiusKm,@Size(max=255) String addressDetail,@Size(max=64) String province,@Size(max=64) String city,@Size(max=64) String district,@DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,@DecimalMin("-90") @DecimalMax("90") BigDecimal latitude) {}

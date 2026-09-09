package com.example.home_service_backend.vo.merchant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ShopView(Long id, Long merchantUserId, String shopName, String shopLogoUrl, String shopLogoObjectKey,
                       String shopIntro, String contactPhone, String serviceArea, BigDecimal serviceRadiusKm,
                       String addressDetail, String province, String city, String district,
                       BigDecimal longitude, BigDecimal latitude, BigDecimal distanceKm, String status,
                       LocalDateTime createdAt, LocalDateTime updatedAt,
                       List<String> serviceNames, List<String> tags, List<String> galleryUrls) {}

package com.example.home_service_backend.vo.merchant;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record MerchantApplicationView(Long id,String applyNo,Long userId,String realName,String phone,String shopName,String intro,String serviceArea,BigDecimal serviceRadiusKm,String addressDetail,String province,String city,String district,BigDecimal longitude,BigDecimal latitude,String status,Long reviewerId,String reviewRemark,LocalDateTime appliedAt,LocalDateTime reviewedAt,List<ApplicationImageView> images,List<Long> serviceIds,List<String> serviceNames,List<String> tags,List<Long> categoryIds,List<MerchantServiceItemView> serviceItems) {}

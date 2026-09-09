package com.example.home_service_backend.vo.address;
import java.math.BigDecimal;
public record AddressView(Long id,String receiverName,String receiverPhone,String province,String city,String district,String detail,BigDecimal longitude,BigDecimal latitude) {}

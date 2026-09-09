package com.example.home_service_backend.vo.map;

import java.math.BigDecimal;
public record GeocodeView(String formattedAddress, String province, String city, String district,
                          String adcode, BigDecimal longitude, BigDecimal latitude) {}

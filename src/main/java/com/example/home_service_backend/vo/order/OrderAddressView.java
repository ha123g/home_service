package com.example.home_service_backend.vo.order;

import java.math.BigDecimal;

/** 订单创建时保存的服务地址快照。 */
public record OrderAddressView(
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String district,
        String detail,
        BigDecimal longitude,
        BigDecimal latitude) {
}

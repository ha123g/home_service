package com.example.home_service_backend.vo.appointment;

import java.math.BigDecimal;

/** 仅向预约参与方展示的服务地址信息。 */
public record AppointmentAddressView(
        Long id,
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String district,
        String detail,
        BigDecimal longitude,
        BigDecimal latitude) {
}

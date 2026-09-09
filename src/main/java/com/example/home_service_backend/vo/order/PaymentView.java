package com.example.home_service_backend.vo.order;
import java.math.BigDecimal;
public record PaymentView(Long paymentId, String orderNo, String provider, String providerPaymentId, BigDecimal amount, String status, String checkoutUrl) {}

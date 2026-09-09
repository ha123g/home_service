package com.example.home_service_backend.common.constants;

import java.util.Set;

public final class OrderConstants {
    public static final String APPOINTMENT_PENDING = "PENDING_PLATFORM";
    public static final String APPOINTMENT_CONTACTING = "CONTACTING";
    public static final String APPOINTMENT_WORKER_ARRANGED = "WORKER_ARRANGED";
    public static final String APPOINTMENT_ORDER_PENDING = "ORDER_PENDING";
    public static final String APPOINTMENT_COMPLETED = "COMPLETED";
    public static final String APPOINTMENT_CANCELLED = "CANCELLED";
    public static final String APPOINTMENT_CLOSED = "CLOSED";
    public static final String ORDER_PENDING_PUBLISH = "PENDING_PUBLISH";
    public static final String ORDER_PENDING_PAYMENT = "PENDING_PAYMENT";
    public static final String ORDER_PAID = "PAID";
    public static final String ORDER_CONFIRMED = "CONFIRMED";
    public static final String ORDER_IN_SERVICE = "IN_SERVICE";
    public static final String ORDER_COMPLETED = "COMPLETED";
    public static final String ORDER_CANCELLED = "CANCELLED";
    public static final String ORDER_CLOSED = "CLOSED";
    public static final Set<String> ORDER_STATUSES = Set.of(ORDER_PENDING_PUBLISH, ORDER_PENDING_PAYMENT,
            ORDER_PAID, ORDER_CONFIRMED, ORDER_IN_SERVICE, ORDER_COMPLETED, ORDER_CANCELLED, ORDER_CLOSED);
    private OrderConstants() {}
}

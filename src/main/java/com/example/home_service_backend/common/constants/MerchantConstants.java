package com.example.home_service_backend.common.constants;

import java.util.Set;

public final class MerchantConstants {
    public static final String APPLICATION_PENDING = "PENDING";
    public static final String APPLICATION_APPROVED = "APPROVED";
    public static final String APPLICATION_REJECTED = "REJECTED";
    public static final String APPLICATION_CANCELLED = "CANCELLED";
    public static final String SHOP_OPEN = "OPEN";
    public static final String SHOP_CLOSED = "CLOSED";
    public static final String SHOP_SUSPENDED = "SUSPENDED";
    public static final Set<String> APPLICATION_STATUSES = Set.of(
            APPLICATION_PENDING, APPLICATION_APPROVED, APPLICATION_REJECTED, APPLICATION_CANCELLED);
    public static final Set<String> REVIEW_DECISIONS = Set.of(APPLICATION_APPROVED, APPLICATION_REJECTED);
    public static final Set<String> SHOP_STATUSES = Set.of(SHOP_OPEN, SHOP_CLOSED, SHOP_SUSPENDED);
    public static final double MAX_NEARBY_RADIUS_KM = 500D;

    private MerchantConstants() {}
}

package com.example.home_service_backend.common.constants;

/** 认证相关常量，避免在 Controller/Service 中散落魔法值。 */
public final class AuthConstants {
    public static final int SESSION_TIMEOUT_SECONDS = 3 * 24 * 60 * 60;
    public static final String DEFAULT_USER_ROLE = "username_normal_user";
    public static final String ROLE_PREFIX = "ROLE_";

    private AuthConstants() {
    }
}

package com.example.home_service_backend.common.utils;

import com.example.home_service_backend.common.exception.BusinessException;
import com.example.home_service_backend.common.result.ResultFactory;
import com.example.home_service_backend.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** 认证上下文访问工具；不承载业务权限规则。 */
public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static LoginUser requireLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(ResultFactory.UNAUTHORIZED_CODE, "未登录或会话已失效");
        }
        return loginUser;
    }
}

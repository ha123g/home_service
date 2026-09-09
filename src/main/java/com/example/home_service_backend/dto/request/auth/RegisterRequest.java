package com.example.home_service_backend.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// record表示不可变的数据载体
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 64, message = "用户名长度为 3-64 个字符")
        @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", message = "用户名仅允许字母、数字、下划线或中文")
        String username,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度为 6-64 个字符")
        String password,
        @Size(max = 64, message = "昵称最长 64 个字符") String nickname) {
}

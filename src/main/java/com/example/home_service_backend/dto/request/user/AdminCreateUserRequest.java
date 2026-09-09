package com.example.home_service_backend.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminCreateUserRequest(
        @NotBlank @Size(min = 3, max = 64)
        @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$") String username,
        @NotBlank @Size(min = 6, max = 64) String password,
        @Size(max = 64) String nickname) {
}

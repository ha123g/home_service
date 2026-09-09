package com.example.home_service_backend.dto.request.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @Size(max = 64) String nickname,
        @Size(max = 512) String avatarUrl,
        @Pattern(regexp = "^$|UNKNOWN|MALE|FEMALE", message = "性别值不合法") String gender) {
}

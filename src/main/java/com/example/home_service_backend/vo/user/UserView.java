package com.example.home_service_backend.vo.user;

import java.time.LocalDateTime;

public record UserView(Long id, String username, String status, String nickname,
                       String avatarUrl, String gender, LocalDateTime lockTime,
                       LocalDateTime createdTime, LocalDateTime updatedTime) {
}

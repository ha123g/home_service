package com.example.home_service_backend.vo.auth;

import java.util.List;

public record LoginResponse(Long userId, String username, List<String> authorities) {
}

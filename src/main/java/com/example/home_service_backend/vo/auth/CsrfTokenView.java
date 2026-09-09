package com.example.home_service_backend.vo.auth;

public record CsrfTokenView(String headerName, String parameterName, String token) {
}

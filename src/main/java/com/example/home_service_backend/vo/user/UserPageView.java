package com.example.home_service_backend.vo.user;

import java.util.List;

public record UserPageView(List<UserView> items, long total, int page, int size) {
}

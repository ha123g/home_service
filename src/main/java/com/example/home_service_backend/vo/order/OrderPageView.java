package com.example.home_service_backend.vo.order;
import java.util.List;
public record OrderPageView(List<OrderView> items, long total, int page, int size) {}

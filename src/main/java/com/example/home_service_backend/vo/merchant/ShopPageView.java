package com.example.home_service_backend.vo.merchant;
import java.util.List;
public record ShopPageView(List<ShopView> items,long total,int page,int size) {}

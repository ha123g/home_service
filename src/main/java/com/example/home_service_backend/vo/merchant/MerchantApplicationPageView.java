package com.example.home_service_backend.vo.merchant;

import java.util.List;
public record MerchantApplicationPageView(List<MerchantApplicationView> items, long total,
                                          int page, int size) {}

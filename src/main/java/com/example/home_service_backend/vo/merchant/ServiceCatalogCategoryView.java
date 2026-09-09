package com.example.home_service_backend.vo.merchant;

import java.util.List;

/** 主页同源的服务分类树，叶子节点为可选择的商家服务。 */
public record ServiceCatalogCategoryView(Long id, String name,
                                         List<ServiceListingView> services,
                                         List<ServiceCatalogCategoryView> children) {}

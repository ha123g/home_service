package com.example.home_service_backend.vo.merchant;

import java.util.List;
public record ServiceCategoryTreeView(Long id, Long parentId, String name,
                                      List<ServiceCategoryTreeView> children) {}

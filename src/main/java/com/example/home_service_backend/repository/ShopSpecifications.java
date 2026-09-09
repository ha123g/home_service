package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.Shop;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

/**
 * 商铺结构化筛选条件。半径查询使用 ShopRepository 中的原生空间查询。
 */
public final class ShopSpecifications {
    private ShopSpecifications() {
    }

    public static Specification<Shop> byFilters(String keyword, String province, String city,
                                                 String district, String status, Collection<Long> shopIds) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("shopName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("shopIntro")), pattern)));
            }
            if (province != null && !province.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("province"), province.trim()));
            }
            if (city != null && !city.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("city"), city.trim()));
            }
            if (district != null && !district.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("district"), district.trim()));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status.trim().toUpperCase()));
            }
            if (shopIds != null) {
                predicates.add(root.get("id").in(shopIds));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}

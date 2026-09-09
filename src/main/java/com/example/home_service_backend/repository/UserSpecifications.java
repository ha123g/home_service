package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * 用户查询条件集中定义在 repository 层，避免在应用服务中拼装持久化表达式。
 */
public final class UserSpecifications {
    private UserSpecifications() {
    }

    public static Specification<User> byKeywordAndStatus(String keyword, String status) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")),
                        "%" + keyword.trim().toLowerCase() + "%"));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status.trim()));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}

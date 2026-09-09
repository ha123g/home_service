package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRoleIdIn(Collection<Long> roleIds);
}

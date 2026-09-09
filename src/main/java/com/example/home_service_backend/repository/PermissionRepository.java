package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByIdIn(Collection<Long> ids);
}

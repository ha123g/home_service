package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.ServiceTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ServiceTagRepository extends JpaRepository<ServiceTag, Long> {
    Optional<ServiceTag> findByName(String name);
    List<ServiceTag> findByNameIn(Collection<String> names);
}

package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.ServiceListingTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ServiceListingTagRepository extends JpaRepository<ServiceListingTag, Long> {
    List<ServiceListingTag> findByServiceIdIn(Collection<Long> serviceIds);
}

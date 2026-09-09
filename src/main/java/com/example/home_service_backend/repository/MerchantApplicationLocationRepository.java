package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.MerchantApplicationLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MerchantApplicationLocationRepository extends JpaRepository<MerchantApplicationLocation, Long> {
    Optional<MerchantApplicationLocation> findByApplicationId(Long applicationId);

    List<MerchantApplicationLocation> findByApplicationIdIn(Collection<Long> ids);
}

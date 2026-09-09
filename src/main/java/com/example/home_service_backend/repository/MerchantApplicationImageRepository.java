package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.MerchantApplicationImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MerchantApplicationImageRepository extends JpaRepository<MerchantApplicationImage, Long> {
    List<MerchantApplicationImage> findByApplicationIdAndStatusOrderById(Long applicationId, String status);

    List<MerchantApplicationImage> findByApplicationIdInAndStatusOrderById(Collection<Long> applicationIds,
                                                                            String status);
}

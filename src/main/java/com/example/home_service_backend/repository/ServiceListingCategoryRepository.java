package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.ServiceListingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface ServiceListingCategoryRepository extends JpaRepository<ServiceListingCategory, Long> {
    @Query("select distinct x.serviceId from ServiceListingCategory x where x.categoryId in :categoryIds")
    List<Long> findServiceIdsByCategoryIds(@Param("categoryIds") Collection<Long> categoryIds);
    List<ServiceListingCategory> findByServiceIdIn(Collection<Long> serviceIds);
}

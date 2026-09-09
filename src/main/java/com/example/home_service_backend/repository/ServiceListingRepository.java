package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.ServiceListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface ServiceListingRepository extends JpaRepository<ServiceListing, Long> {
    List<ServiceListing> findByShopIdAndStatusOrderByCreatedTimeDesc(Long shopId, String status);
    List<ServiceListing> findByStatusOrderByTitleAsc(String status);
    @Query("select distinct s.shopId from ServiceListing s where s.status = 'ONLINE' and s.shopId is not null and (lower(s.title) like lower(concat('%', :keyword, '%')) or lower(s.summary) like lower(concat('%', :keyword, '%')))")
    List<Long> findOnlineShopIdsByKeyword(@Param("keyword") String keyword);
    @Query("select distinct s.shopId from ServiceListing s where s.id in :ids and s.status = 'ONLINE' and s.shopId is not null")
    List<Long> findOnlineShopIdsByIds(@Param("ids") Collection<Long> ids);
}

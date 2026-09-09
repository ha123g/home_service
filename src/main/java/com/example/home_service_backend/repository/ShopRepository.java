package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long>, JpaSpecificationExecutor<Shop> {
    long countByStatus(String status);
    Optional<Shop> findByMerchantUserId(Long merchantUserId);
    @Query("select s.id from Shop s where lower(s.shopName) like lower(concat('%', :keyword, '%')) or lower(s.shopIntro) like lower(concat('%', :keyword, '%')) or lower(s.serviceArea) like lower(concat('%', :keyword, '%'))")
    java.util.List<Long> findIdsByKeyword(@Param("keyword") String keyword);
    @Query(value = "SELECT * FROM shop s WHERE s.status = :status AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL "
            + "AND (:keyword IS NULL OR LOWER(s.shop_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.shop_intro) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:province IS NULL OR s.province = :province) AND (:city IS NULL OR s.city = :city) AND (:district IS NULL OR s.district = :district) "
            + "AND (:filterIds = false OR s.id IN (:shopIds)) "
            + "AND s.latitude BETWEEN :minLat AND :maxLat AND s.longitude BETWEEN :minLon AND :maxLon "
            + "AND ST_Distance_Sphere(POINT(s.longitude, s.latitude), POINT(:lon, :lat)) <= (:km * 1000) "
            + "AND (s.service_radius_km IS NULL OR ST_Distance_Sphere(POINT(s.longitude, s.latitude), POINT(:lon, :lat)) <= (s.service_radius_km * 1000)) "
            + "ORDER BY ST_Distance_Sphere(POINT(s.longitude, s.latitude), POINT(:lon, :lat))",
            countQuery = "SELECT COUNT(*) FROM shop s WHERE s.status = :status AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL "
                    + "AND (:keyword IS NULL OR LOWER(s.shop_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.shop_intro) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
                    + "AND (:province IS NULL OR s.province = :province) AND (:city IS NULL OR s.city = :city) AND (:district IS NULL OR s.district = :district) "
                    + "AND (:filterIds = false OR s.id IN (:shopIds)) "
                    + "AND s.latitude BETWEEN :minLat AND :maxLat AND s.longitude BETWEEN :minLon AND :maxLon "
                    + "AND ST_Distance_Sphere(POINT(s.longitude, s.latitude), POINT(:lon, :lat)) <= (:km * 1000) "
                    + "AND (s.service_radius_km IS NULL OR ST_Distance_Sphere(POINT(s.longitude, s.latitude), POINT(:lon, :lat)) <= (s.service_radius_km * 1000))",
            nativeQuery = true)
    Page<Shop> findNearby(@Param("lat") BigDecimal lat, @Param("lon") BigDecimal lon, @Param("km") double km,
                          @Param("status") String status, @Param("keyword") String keyword,
                          @Param("province") String province, @Param("city") String city,
                          @Param("district") String district, @Param("filterIds") boolean filterIds,
                          @Param("shopIds") Collection<Long> shopIds,
                          @Param("minLat") BigDecimal minLat, @Param("maxLat") BigDecimal maxLat,
                          @Param("minLon") BigDecimal minLon, @Param("maxLon") BigDecimal maxLon,
                          Pageable pageable);
}

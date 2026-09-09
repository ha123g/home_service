package com.example.home_service_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 入驻申请(MerchantApplication)实体类
 */
@Entity
@Table(name = "merchant_application")
public class MerchantApplication implements Serializable {
    private static final long serialVersionUID = -91344721073666621L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "apply_no", length = 64)
    private String applyNo;

    @Column(name = "real_name", nullable = false, length = 64)
    private String realName;

    @Column(length = 32)
    private String phone;

    @Column(length = 1024)
    private String intro;

    @Column(name = "shop_name", length = 128)
    private String shopName;

    @Column(name = "service_area", length = 255)
    private String serviceArea;

    @Column(name = "service_radius_km", precision = 6, scale = 2)
    private java.math.BigDecimal serviceRadiusKm;

    @Column(name = "service_ids", length = 1024)
    private String serviceIds;

    @Column(length = 512)
    private String tags;

    @Column(name = "service_category_ids", length = 1024)
    private String serviceCategoryIds;

    @Column(name = "service_extensions", columnDefinition = "longtext")
    private String serviceExtensions;

    @Column(name = "address_detail", length = 255)
    private String addressDetail;

    @Column(length = 64)
    private String province;

    @Column(length = 64)
    private String city;

    @Column(length = 64)
    private String district;

    @Column(precision = 10, scale = 7)
    private java.math.BigDecimal longitude;

    @Column(precision = 10, scale = 7)
    private java.math.BigDecimal latitude;

    @Column(name = "logo_object_key", length = 512)
    private String logoObjectKey;

    @Column(name = "review_remark", length = 512)
    private String reviewRemark;

    /** PENDING/APPROVED/REJECTED/CANCELLED */
    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getApplyNo() {
        return applyNo;
    }

    public void setApplyNo(String applyNo) {
        this.applyNo = applyNo;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getServiceArea() { return serviceArea; }
    public void setServiceArea(String serviceArea) { this.serviceArea = serviceArea; }
    public java.math.BigDecimal getServiceRadiusKm() { return serviceRadiusKm; }
    public void setServiceRadiusKm(java.math.BigDecimal serviceRadiusKm) { this.serviceRadiusKm = serviceRadiusKm; }
    public String getServiceIds() { return serviceIds; }
    public void setServiceIds(String serviceIds) { this.serviceIds = serviceIds; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getServiceCategoryIds() { return serviceCategoryIds; }
    public void setServiceCategoryIds(String serviceCategoryIds) { this.serviceCategoryIds = serviceCategoryIds; }
    public String getServiceExtensions() { return serviceExtensions; }
    public void setServiceExtensions(String serviceExtensions) { this.serviceExtensions = serviceExtensions; }
    public String getAddressDetail() { return addressDetail; }
    public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public java.math.BigDecimal getLongitude() { return longitude; }
    public void setLongitude(java.math.BigDecimal longitude) { this.longitude = longitude; }
    public java.math.BigDecimal getLatitude() { return latitude; }
    public void setLatitude(java.math.BigDecimal latitude) { this.latitude = latitude; }
    public String getLogoObjectKey() { return logoObjectKey; }
    public void setLogoObjectKey(String logoObjectKey) { this.logoObjectKey = logoObjectKey; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

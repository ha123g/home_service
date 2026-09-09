package com.example.home_service_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务商店铺(Shop)实体类
 */
@Entity
@Table(name = "shop")
public class Shop implements Serializable {
    private static final long serialVersionUID = -99066973942325995L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_user_id", nullable = false)
    private Long merchantUserId;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "shop_name", length = 128)
    private String shopName;

    @Column(name = "shop_logo_url", length = 512)
    private String shopLogoUrl;

    @Column(name = "shop_logo_object_key", length = 512)
    private String shopLogoObjectKey;

    @Column(name = "shop_intro", length = 2048)
    private String shopIntro;

    @Column(name = "contact_phone", length = 32)
    private String contactPhone;

    @Column(name = "service_area", length = 255)
    private String serviceArea;

    @Column(name = "service_radius_km", precision = 6, scale = 2)
    private BigDecimal serviceRadiusKm;

    @Column(name = "address_detail", length = 255)
    private String addressDetail;

    @Column(length = 64)
    private String province;

    @Column(length = 64)
    private String city;

    @Column(length = 64)
    private String district;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    /** OPEN/CLOSED/SUSPENDED */
    @Column(length = 16)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMerchantUserId() {
        return merchantUserId;
    }

    public void setMerchantUserId(Long merchantUserId) {
        this.merchantUserId = merchantUserId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopLogoUrl() {
        return shopLogoUrl;
    }

    public void setShopLogoUrl(String shopLogoUrl) {
        this.shopLogoUrl = shopLogoUrl;
    }

    public String getShopLogoObjectKey() { return shopLogoObjectKey; }
    public void setShopLogoObjectKey(String shopLogoObjectKey) { this.shopLogoObjectKey = shopLogoObjectKey; }

    public String getShopIntro() {
        return shopIntro;
    }

    public void setShopIntro(String shopIntro) {
        this.shopIntro = shopIntro;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public BigDecimal getServiceRadiusKm() { return serviceRadiusKm; }

    public void setServiceRadiusKm(BigDecimal serviceRadiusKm) { this.serviceRadiusKm = serviceRadiusKm; }

    public String getAddressDetail() {
        return addressDetail;
    }

    public void setAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
    }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

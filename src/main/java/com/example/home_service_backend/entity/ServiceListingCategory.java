package com.example.home_service_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

/**
 * 服务分类关联(ServiceListingCategory)实体类
 */
@Entity
@Table(name = "service_listing_category")
public class ServiceListingCategory implements Serializable {
    private static final long serialVersionUID = 844743026531527827L;

    @Id
    private Long id;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "category_id")
    private Long categoryId;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

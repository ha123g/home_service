package com.example.home_service_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

/**
 * 服务标签关联(ServiceListingTag)实体类
 */
@Entity
@Table(name = "service_listing_tag")
public class ServiceListingTag implements Serializable {
    private static final long serialVersionUID = -89374240581159266L;

    @Id
    private Long id;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "tag_id")
    private Long tagId;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

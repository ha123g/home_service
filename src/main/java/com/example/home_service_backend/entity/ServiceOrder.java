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
 * 服务订单表(ServiceOrder)实体类
 */
@Entity
@Table(name = "service_order")
public class ServiceOrder implements Serializable {
    private static final long serialVersionUID = 137355845944395432L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, length = 64)
    private String orderNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "worker_id")
    private Long workerId;

    @Column(name = "address_id", nullable = false)
    private Long addressId;

    @Column(name = "scheduled_start")
    private LocalDateTime scheduledStart;

    @Column(name = "scheduled_end")
    private LocalDateTime scheduledEnd;

    @Column(name = "service_address_snapshot", columnDefinition = "json")
    private String serviceAddressSnapshot;

    @Column(name = "service_title_snapshot", length = 128)
    private String serviceTitleSnapshot;

    @Column(name = "worker_name_snapshot", length = 64)
    private String workerNameSnapshot;

    @Column(name = "origin_amount", precision = 12, scale = 2)
    private BigDecimal originAmount;

    @Column(name = "payable_amount", precision = 12, scale = 2)
    private BigDecimal payableAmount;

    @Column(name = "source_request_id")
    private Long sourceRequestId;

    @Column(name = "published_time")
    private LocalDateTime publishedTime;

    @Column(name = "confirmed_time")
    private LocalDateTime confirmedTime;

    /** PENDING_PAYMENT/PAID/CONFIRMED/IN_SERVICE/COMPLETED/CANCELLED/CLOSED */
    @Column(nullable = false, length = 24)
    private String status;

    @Column(name = "cancel_reason", length = 512)
    private String cancelReason;

    @Column(length = 512)
    private String remark;

    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    @Column(name = "paid_time")
    private LocalDateTime paidTime;

    @Column(name = "cancelled_time")
    private LocalDateTime cancelledTime;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public LocalDateTime getScheduledStart() {
        return scheduledStart;
    }

    public void setScheduledStart(LocalDateTime scheduledStart) {
        this.scheduledStart = scheduledStart;
    }

    public LocalDateTime getScheduledEnd() {
        return scheduledEnd;
    }

    public void setScheduledEnd(LocalDateTime scheduledEnd) {
        this.scheduledEnd = scheduledEnd;
    }

    public String getServiceAddressSnapshot() {
        return serviceAddressSnapshot;
    }

    public void setServiceAddressSnapshot(String serviceAddressSnapshot) {
        this.serviceAddressSnapshot = serviceAddressSnapshot;
    }

    public String getServiceTitleSnapshot() {
        return serviceTitleSnapshot;
    }

    public void setServiceTitleSnapshot(String serviceTitleSnapshot) {
        this.serviceTitleSnapshot = serviceTitleSnapshot;
    }

    public String getWorkerNameSnapshot() {
        return workerNameSnapshot;
    }

    public void setWorkerNameSnapshot(String workerNameSnapshot) {
        this.workerNameSnapshot = workerNameSnapshot;
    }

    public BigDecimal getOriginAmount() {
        return originAmount;
    }

    public void setOriginAmount(BigDecimal originAmount) {
        this.originAmount = originAmount;
    }

    public BigDecimal getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(BigDecimal payableAmount) {
        this.payableAmount = payableAmount;
    }

    public Long getSourceRequestId() {
        return sourceRequestId;
    }

    public void setSourceRequestId(Long sourceRequestId) {
        this.sourceRequestId = sourceRequestId;
    }

    public LocalDateTime getPublishedTime() {
        return publishedTime;
    }

    public void setPublishedTime(LocalDateTime publishedTime) {
        this.publishedTime = publishedTime;
    }

    public LocalDateTime getConfirmedTime() {
        return confirmedTime;
    }

    public void setConfirmedTime(LocalDateTime confirmedTime) {
        this.confirmedTime = confirmedTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public LocalDateTime getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(LocalDateTime paidTime) {
        this.paidTime = paidTime;
    }

    public LocalDateTime getCancelledTime() {
        return cancelledTime;
    }

    public void setCancelledTime(LocalDateTime cancelledTime) {
        this.cancelledTime = cancelledTime;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}

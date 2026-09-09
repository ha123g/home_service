package com.example.home_service_backend.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 用户预约申请；平台和申请用户均可查看。 */
@Entity
@Table(name = "appointment_request")
public class AppointmentRequest implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_no", nullable = false, length = 64)
    private String requestNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "address_id", nullable = false)
    private Long addressId;

    @Column(name = "service_title_input", length = 128)
    private String serviceTitleInput;

    @Column(name = "requirement_text", nullable = false, length = 2048)
    private String requirementText;
    @Column(name = "preferred_start")
    private LocalDateTime preferredStart;

    @Column(name = "preferred_end")
    private LocalDateTime preferredEnd;

    @Column(name = "contact_name", length = 64)
    private String contactName;

    @Column(name = "contact_phone", length = 32)
    private String contactPhone;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "platform_operator_id")
    private Long platformOperatorId;

    @Column(name = "platform_note", length = 2048)
    private String platformNote;

    @Column(name = "cancel_reason", length = 512)
    private String cancelReason;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public String getServiceTitleInput() { return serviceTitleInput; }
    public void setServiceTitleInput(String serviceTitleInput) { this.serviceTitleInput = serviceTitleInput; }
    public String getRequirementText() { return requirementText; }
    public void setRequirementText(String requirementText) { this.requirementText = requirementText; }
    public LocalDateTime getPreferredStart() { return preferredStart; }
    public void setPreferredStart(LocalDateTime preferredStart) { this.preferredStart = preferredStart; }
    public LocalDateTime getPreferredEnd() { return preferredEnd; }
    public void setPreferredEnd(LocalDateTime preferredEnd) { this.preferredEnd = preferredEnd; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getPlatformOperatorId() { return platformOperatorId; }
    public void setPlatformOperatorId(Long platformOperatorId) { this.platformOperatorId = platformOperatorId; }
    public String getPlatformNote() { return platformNote; }
    public void setPlatformNote(String platformNote) { this.platformNote = platformNote; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
}

package com.example.home_service_backend.service;

import com.example.home_service_backend.common.constants.OrderConstants;
import com.example.home_service_backend.common.exception.BusinessException;
import com.example.home_service_backend.common.utils.SecurityUtils;
import com.example.home_service_backend.config.StripeProperties;
import com.example.home_service_backend.dto.request.order.CreateOrderRequest;
import com.example.home_service_backend.dto.request.order.PayOrderRequest;
import com.example.home_service_backend.dto.request.order.PublishOrderRequest;
import com.example.home_service_backend.dto.request.order.UpdateOrderStatusRequest;
import com.example.home_service_backend.entity.*;
import com.example.home_service_backend.repository.*;
import com.example.home_service_backend.security.LoginUser;
import com.example.home_service_backend.vo.order.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 订单生成、发布和 Stripe 沙盒支付编排；不包含工作人员管理。 */
@Service
public class OrderService {
    private final ServiceOrderRepository orderRepository;
    private final ServiceOrderStatusHistoryRepository historyRepository;
    private final PaymentRecordRepository paymentRepository;
    private final AppointmentRequestRepository appointmentRepository;
    private final AppointmentService appointmentService;
    private final ShopRepository shopRepository;
    private final ServiceListingRepository listingRepository;
    private final AddressRepository addressRepository;
    private final ObjectMapper objectMapper;
    private final StripeProperties stripe;
    private final StripeSandboxPaymentGateway paymentGateway;
    private final TransactionTemplate transactionTemplate;

    public OrderService(ServiceOrderRepository orderRepository, ServiceOrderStatusHistoryRepository historyRepository,
                        PaymentRecordRepository paymentRepository, AppointmentRequestRepository appointmentRepository,
                        AppointmentService appointmentService, ShopRepository shopRepository,
                        ServiceListingRepository listingRepository, AddressRepository addressRepository,
                        ObjectMapper objectMapper, StripeProperties stripe, StripeSandboxPaymentGateway paymentGateway,
                        org.springframework.transaction.PlatformTransactionManager transactionManager) {
        this.orderRepository = orderRepository; this.historyRepository = historyRepository; this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository; this.appointmentService = appointmentService; this.shopRepository = shopRepository;
        this.listingRepository = listingRepository; this.addressRepository = addressRepository; this.objectMapper = objectMapper; this.stripe = stripe; this.paymentGateway = paymentGateway;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /** 平台根据线下估价生成待发布订单。 */
    @Transactional
    public OrderView createFromAppointment(CreateOrderRequest request) {
        LoginUser operator = SecurityUtils.requireLoginUser();
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            // 业务调用方应复用幂等键；订单来源预约唯一索引确保重复生成返回既有订单。
        }
        AppointmentRequest appointment = appointmentService.find(request.appointmentId());
        requirePlatformOrShopOwner(operator, appointment.getShopId());
        if (!(OrderConstants.APPOINTMENT_WORKER_ARRANGED.equals(appointment.getStatus()) || OrderConstants.APPOINTMENT_ORDER_PENDING.equals(appointment.getStatus()))) throw new BusinessException("409", "预约当前不可生成订单");
        var existing = orderRepository.findBySourceRequestId(appointment.getId());
        if (existing.isPresent()) return toView(existing.get());
        ServiceListing listing = listingRepository.findById(appointment.getServiceId()).orElseThrow(() -> new BusinessException("404", "服务项目不存在"));
        Address address = addressRepository.findById(appointment.getAddressId()).orElseThrow(() -> new BusinessException("404", "预约地址不存在"));
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        ServiceOrder order = new ServiceOrder(); order.setOrderNo("SO" + UUID.randomUUID().toString().replace("-", "").substring(0, 24)); order.setUserId(appointment.getUserId()); order.setShopId(appointment.getShopId()); order.setServiceId(appointment.getServiceId()); order.setAddressId(appointment.getAddressId()); order.setScheduledStart(request.scheduledStart() == null ? appointment.getPreferredStart() : request.scheduledStart()); order.setScheduledEnd(request.scheduledEnd() == null ? appointment.getPreferredEnd() : request.scheduledEnd()); order.setServiceTitleSnapshot(listing.getTitle()); order.setWorkerId(request.workerId()); order.setWorkerNameSnapshot(request.workerName()); order.setOriginAmount(request.amount()); order.setPayableAmount(request.amount()); order.setSourceRequestId(appointment.getId()); order.setStatus(OrderConstants.ORDER_PENDING_PUBLISH); order.setRemark(request.remark()); order.setCreatedTime(now); order.setUpdatedTime(now);
        try { Map<String,Object> snapshot = new LinkedHashMap<>(); snapshot.put("receiverName", address.getReceiverName()); snapshot.put("receiverPhone", address.getReceiverPhone()); snapshot.put("province", address.getProvince()); snapshot.put("city", address.getCity()); snapshot.put("district", address.getDistrict()); snapshot.put("detail", address.getDetail()); snapshot.put("longitude", address.getLongitude()); snapshot.put("latitude", address.getLatitude()); order.setServiceAddressSnapshot(objectMapper.writeValueAsString(snapshot)); } catch (JsonProcessingException e) { throw new BusinessException("500", "地址快照生成失败"); }
        order = orderRepository.save(order); record(order, null, order.getStatus(), operator.getId(), "平台根据现场估价生成订单", request.idempotencyKey());
        if (OrderConstants.APPOINTMENT_WORKER_ARRANGED.equals(appointment.getStatus())) appointmentService.transition(appointment, OrderConstants.APPOINTMENT_ORDER_PENDING, operator.getId(), "已生成订单");
        return toView(order);
    }

    @Transactional
    public OrderView publish(Long id, PublishOrderRequest request) { LoginUser operator = SecurityUtils.requireLoginUser(); ServiceOrder order = find(id); requirePlatformOrShopOwner(operator, order.getShopId()); if (!OrderConstants.ORDER_PENDING_PUBLISH.equals(order.getStatus())) throw new BusinessException("409", "订单当前不可发布"); long minutes = request == null || request.expireMinutes() <= 0 ? stripe.expireMinutes() : request.expireMinutes(); if (minutes <= 0) minutes = 30; LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC); order.setStatus(OrderConstants.ORDER_PENDING_PAYMENT); order.setPublishedTime(now); order.setExpireTime(now.plusMinutes(Math.min(minutes, 1440))); order.setUpdatedTime(now); orderRepository.save(order); record(order, OrderConstants.ORDER_PENDING_PUBLISH, order.getStatus(), operator.getId(), "商家发布订单", null); return toView(order); }

    /** Stripe 沙盒模式：创建本地支付记录并返回可配置的 Checkout 地址。 */
    public PaymentView pay(Long id, PayOrderRequest request) { LoginUser user = SecurityUtils.requireLoginUser(); ServiceOrder order = find(id); if (!order.getUserId().equals(user.getId())) throw new BusinessException("403", "无权支付该订单"); String key = request.idempotencyKey().trim(); var previous = paymentRepository.findByIdempotencyKey(key); if (previous.isPresent()) { if (!order.getId().equals(previous.get().getOrderId())) throw new BusinessException("409", "支付请求编号已被其他订单使用"); if ("SUCCESS".equals(previous.get().getStatus())) return toPayment(previous.get()); throw new BusinessException("409", "原支付请求未成功，请重新发起支付"); } if (!OrderConstants.ORDER_PENDING_PAYMENT.equals(order.getStatus())) throw new BusinessException("409", "订单当前不可支付"); if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now(ZoneOffset.UTC))) throw new BusinessException("409", "订单支付已过期"); var session = paymentGateway.create(order.getPayableAmount(), order.getOrderNo(), key); if (!"succeeded".equalsIgnoreCase(session.status())) throw new BusinessException("502", "Stripe 沙盒支付未完成，请稍后重试"); return transactionTemplate.execute(status -> finalizePayment(id, user.getId(), key, session)); }

    private PaymentView finalizePayment(Long id, Long userId, String key, StripeSandboxPaymentGateway.PaymentSession session) { ServiceOrder order = find(id); if (!order.getUserId().equals(userId)) throw new BusinessException("403", "无权支付该订单"); var previous = paymentRepository.findByIdempotencyKey(key); if (previous.isPresent()) { if (!order.getId().equals(previous.get().getOrderId())) throw new BusinessException("409", "支付请求编号已被其他订单使用"); if ("SUCCESS".equals(previous.get().getStatus())) return toPayment(previous.get()); throw new BusinessException("409", "原支付请求未成功，请重新发起支付"); } if (!OrderConstants.ORDER_PENDING_PAYMENT.equals(order.getStatus())) throw new BusinessException("409", "订单当前不可支付"); PaymentRecord payment = new PaymentRecord(); payment.setOrderId(order.getId()); payment.setOrderNo(order.getOrderNo()); payment.setProviderPaymentId(session.providerPaymentId()); payment.setIdempotencyKey(key); payment.setChannel("STRIPE"); payment.setMethod("CARD"); payment.setStatus("SUCCESS"); payment.setAmount(order.getPayableAmount()); payment.setPaidTime(LocalDateTime.now(ZoneOffset.UTC)); payment.setCreatedTime(LocalDateTime.now(ZoneOffset.UTC)); payment.setUpdatedTime(payment.getCreatedTime()); payment = paymentRepository.save(payment); order.setStatus(OrderConstants.ORDER_PAID); order.setPaidTime(payment.getPaidTime()); order.setUpdatedTime(payment.getPaidTime()); orderRepository.save(order); record(order, OrderConstants.ORDER_PENDING_PAYMENT, OrderConstants.ORDER_PAID, userId, "Stripe 沙盒支付成功", key); return new PaymentView(payment.getId(), payment.getOrderNo(), "STRIPE_SANDBOX", payment.getProviderPaymentId(), payment.getAmount(), payment.getStatus(), session.checkoutUrl()); }
    @Transactional public OrderView confirm(Long id) { LoginUser u=SecurityUtils.requireLoginUser(); ServiceOrder o=find(id); if(!o.getUserId().equals(u.getId())) throw new BusinessException("403","无权确认该订单"); if(!OrderConstants.ORDER_PAID.equals(o.getStatus())) throw new BusinessException("409","订单尚未支付或已确认"); o.setStatus(OrderConstants.ORDER_CONFIRMED); o.setConfirmedTime(LocalDateTime.now(ZoneOffset.UTC)); o.setUpdatedTime(o.getConfirmedTime()); orderRepository.save(o); record(o,OrderConstants.ORDER_PAID,OrderConstants.ORDER_CONFIRMED,u.getId(),"用户确认订单",null); return toView(o); }

    @Transactional
    public OrderView updateStatus(Long id, UpdateOrderStatusRequest request) {
        LoginUser operator = SecurityUtils.requireLoginUser();
        ServiceOrder order = find(id);
        requirePlatformOrShopOwner(operator, order.getShopId());
        String target = request.status().trim().toUpperCase();
        String old = order.getStatus();
        boolean valid = (OrderConstants.ORDER_CONFIRMED.equals(old) && OrderConstants.ORDER_IN_SERVICE.equals(target))
                || (OrderConstants.ORDER_IN_SERVICE.equals(old) && OrderConstants.ORDER_COMPLETED.equals(target));
        if (!valid) throw new BusinessException("409", "订单当前不可变更为该状态");
        order.setStatus(target); order.setUpdatedTime(LocalDateTime.now(ZoneOffset.UTC));
        orderRepository.save(order); record(order, old, target, operator.getId(), request.reason(), null);
        // 订单完成后同步关闭来源预约，避免预约中心长期停留在“待发布订单”。
        if (OrderConstants.ORDER_COMPLETED.equals(target) && order.getSourceRequestId() != null) {
            appointmentRepository.findById(order.getSourceRequestId()).ifPresent(appointment -> {
                if (OrderConstants.APPOINTMENT_ORDER_PENDING.equals(appointment.getStatus())) {
                    appointmentService.transition(appointment, OrderConstants.APPOINTMENT_CLOSED,
                            operator.getId(), "订单已完成");
                }
            });
        }
        return toView(order);
    }

    @Transactional(readOnly = true)
    public OrderView get(Long id) { ServiceOrder o = find(id); authorizeRead(o); return toView(o); }
    @Transactional(readOnly = true)
    public OrderPageView list(int page, int size) { LoginUser u = SecurityUtils.requireLoginUser(); Pageable p = PageRequest.of(page, size); Page<ServiceOrder> result; if (isPlatform(u)) result = orderRepository.findAllByOrderByCreatedTimeDesc(p); else { var ownedShop = shopRepository.findByMerchantUserId(u.getId()); result = ownedShop.map(shop -> orderRepository.findByUserIdOrShopIdOrderByCreatedTimeDesc(u.getId(), shop.getId(), p)).orElseGet(() -> orderRepository.findByUserIdOrderByCreatedTimeDesc(u.getId(), p)); } return new OrderPageView(result.getContent().stream().map(this::toView).toList(), result.getTotalElements(), page, size); }

    private ServiceOrder find(Long id) { return orderRepository.findById(id).orElseThrow(() -> new BusinessException("404", "订单不存在")); }
    private void authorizeRead(ServiceOrder o) { LoginUser u = SecurityUtils.requireLoginUser(); if (o.getUserId().equals(u.getId()) || isPlatform(u) || shopRepository.findById(o.getShopId()).map(s -> s.getMerchantUserId().equals(u.getId())).orElse(false)) return; throw new BusinessException("403", "无权查看该订单"); }
    private void requirePlatform(LoginUser u) { if (!isPlatform(u)) throw new BusinessException("403", "仅平台管理员可执行该操作"); }
    private void requirePlatformOrShopOwner(LoginUser u, Long shopId) {
        if (isPlatform(u)) return;
        boolean owner = shopRepository.findById(shopId).map(shop -> shop.getMerchantUserId().equals(u.getId())).orElse(false);
        if (!owner) throw new BusinessException("403", "仅商家或平台管理员可处理该订单");
    }
    private boolean isPlatform(LoginUser u) { return u.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_username_super_admin") || a.getAuthority().equals("ROLE_username_sys_admin") || a.getAuthority().equals("ROLE_username_aud_admin")); }
    private void record(ServiceOrder o, String from, String to, Long operator, String reason, String key) { ServiceOrderStatusHistory h = new ServiceOrderStatusHistory(); h.setOrderId(o.getId()); h.setFromStatus(from); h.setToStatus(to); h.setOperatorUserId(operator); h.setReason(reason); h.setIdempotencyKey(key); h.setCreatedTime(LocalDateTime.now(ZoneOffset.UTC)); historyRepository.save(h); }
    private OrderView toView(ServiceOrder o) {
        String provider = paymentRepository.findTopByOrderIdOrderByIdDesc(o.getId())
                .map(PaymentRecord::getProviderPaymentId).orElse(null);
        Shop shop = shopRepository.findById(o.getShopId()).orElse(null);
        ServiceListing listing = listingRepository.findById(o.getServiceId()).orElse(null);
        OrderServiceView service = listing == null
                ? new OrderServiceView(o.getServiceId(), o.getServiceTitleSnapshot(), null, null, null, null, null)
                : new OrderServiceView(listing.getId(), listing.getTitle(), listing.getSummary(), listing.getDescription(),
                listing.getPricingUnit(), listing.getBasePrice(), listing.getDurationMinutes());
        OrderAddressView address = parseAddressSnapshot(o.getServiceAddressSnapshot());
        if (address == null && o.getAddressId() != null) {
            address = addressRepository.findById(o.getAddressId()).map(this::toAddress).orElse(null);
        }
        List<OrderStatusHistoryView> history = historyRepository.findByOrderIdOrderByCreatedTimeAsc(o.getId()).stream()
                .map(item -> new OrderStatusHistoryView(item.getId(), item.getFromStatus(), item.getToStatus(),
                        item.getOperatorUserId(), item.getReason(), item.getCreatedTime()))
                .toList();
        return new OrderView(o.getId(), o.getOrderNo(), o.getUserId(), o.getShopId(), o.getServiceId(),
                o.getWorkerId(), o.getAddressId(), o.getScheduledStart(), o.getScheduledEnd(),
                o.getServiceAddressSnapshot(), o.getServiceTitleSnapshot(), o.getWorkerNameSnapshot(),
                o.getOriginAmount(), o.getPayableAmount(), o.getSourceRequestId(), o.getPublishedTime(),
                o.getConfirmedTime(), o.getStatus(), o.getRemark(), o.getExpireTime(), o.getPaidTime(),
                o.getCancelledTime(), provider, o.getCreatedTime(), o.getUpdatedTime(),
                shop == null ? null : shop.getShopName(), service, address, history);
    }

    private OrderAddressView parseAddressSnapshot(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            var node = objectMapper.readTree(value);
            return new OrderAddressView(text(node, "receiverName"), text(node, "receiverPhone"),
                    text(node, "province"), text(node, "city"), text(node, "district"), text(node, "detail"),
                    decimal(node, "longitude"), decimal(node, "latitude"));
        } catch (JsonProcessingException | RuntimeException ignored) {
            return null;
        }
    }

    private OrderAddressView toAddress(Address address) {
        return new OrderAddressView(address.getReceiverName(), address.getReceiverPhone(), address.getProvince(),
                address.getCity(), address.getDistrict(), address.getDetail(), address.getLongitude(), address.getLatitude());
    }

    private String text(com.fasterxml.jackson.databind.JsonNode node, String name) {
        var value = node.get(name);
        return value == null || value.isNull() ? null : value.asText(null);
    }

    private BigDecimal decimal(com.fasterxml.jackson.databind.JsonNode node, String name) {
        var value = node.get(name);
        if (value == null || value.isNull()) return null;
        if (value.isNumber()) return value.decimalValue();
        try {
            return new BigDecimal(value.asText());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
    private PaymentView toPayment(PaymentRecord p) { String base = stripe.checkoutBaseUrl() == null ? "" : stripe.checkoutBaseUrl(); return new PaymentView(p.getId(), p.getOrderNo(), "STRIPE_SANDBOX", p.getProviderPaymentId(), p.getAmount(), p.getStatus(), base.isBlank() ? null : base + "?paymentId=" + p.getProviderPaymentId()); }
}

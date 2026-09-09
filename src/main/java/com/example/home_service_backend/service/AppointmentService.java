package com.example.home_service_backend.service;

import com.example.home_service_backend.common.constants.OrderConstants;
import com.example.home_service_backend.common.exception.BusinessException;
import com.example.home_service_backend.common.utils.SecurityUtils;
import com.example.home_service_backend.dto.request.appointment.CancelAppointmentRequest;
import com.example.home_service_backend.dto.request.appointment.CreateAppointmentRequest;
import com.example.home_service_backend.dto.request.appointment.UpdateAppointmentStatusRequest;
import com.example.home_service_backend.entity.*;
import com.example.home_service_backend.repository.*;
import com.example.home_service_backend.security.LoginUser;
import com.example.home_service_backend.vo.appointment.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {
    private final AppointmentRequestRepository appointmentRepository;
    private final AppointmentRequestStatusHistoryRepository historyRepository;
    private final AddressRepository addressRepository;
    private final ShopRepository shopRepository;
    private final ServiceListingRepository serviceRepository;
    public AppointmentService(AppointmentRequestRepository appointmentRepository, AppointmentRequestStatusHistoryRepository historyRepository, AddressRepository addressRepository, ShopRepository shopRepository, ServiceListingRepository serviceRepository) { this.appointmentRepository=appointmentRepository; this.historyRepository=historyRepository; this.addressRepository=addressRepository; this.shopRepository=shopRepository; this.serviceRepository=serviceRepository; }

    @Transactional
    public AppointmentView create(CreateAppointmentRequest r) {
        LoginUser u=SecurityUtils.requireLoginUser();
        if (r.idempotencyKey() != null && !r.idempotencyKey().isBlank()) {
            var previous = appointmentRepository.findByUserIdAndIdempotencyKey(u.getId(), r.idempotencyKey().trim());
            if (previous.isPresent()) return get(previous.get().getId());
        }
        if(r.preferredStart()!=null&&r.preferredEnd()!=null&&!r.preferredEnd().isAfter(r.preferredStart())) throw new BusinessException("400","期望结束时间必须晚于开始时间");
        Shop shop=shopRepository.findById(r.shopId()).orElseThrow(()->new BusinessException("404","商铺不存在"));
        if(!"OPEN".equals(shop.getStatus())) throw new BusinessException("409","商铺当前不可预约");
        ServiceListing listing=serviceRepository.findById(r.serviceId()).orElseThrow(()->new BusinessException("404","服务项目不存在"));
        if(!"ONLINE".equals(listing.getStatus())||!r.shopId().equals(listing.getShopId())) throw new BusinessException("409","服务项目不可预约");
        Address address=addressRepository.findByIdAndUserIdAndStatus(r.addressId(),u.getId(),"ACTIVE").orElseThrow(()->new BusinessException("404","服务地址不存在或无权访问"));
        LocalDateTime now=LocalDateTime.now(ZoneOffset.UTC); AppointmentRequest a=new AppointmentRequest(); a.setRequestNo("AP"+UUID.randomUUID().toString().replace("-","").substring(0,24)); a.setUserId(u.getId()); a.setShopId(shop.getId()); a.setServiceId(listing.getId()); a.setAddressId(address.getId()); a.setServiceTitleInput(r.serviceTitleInput()); a.setRequirementText(r.requirementText().trim()); a.setPreferredStart(r.preferredStart()); a.setPreferredEnd(r.preferredEnd()); a.setContactName(r.contactName()); a.setContactPhone(r.contactPhone()); a.setIdempotencyKey(r.idempotencyKey()==null?null:r.idempotencyKey().trim()); a.setStatus(OrderConstants.APPOINTMENT_PENDING); a.setCreatedTime(now); a.setUpdatedTime(now); a=appointmentRepository.save(a); record(a,null,a.getStatus(),u.getId(),"用户提交预约",r.idempotencyKey()); return toView(a);
    }
    @Transactional(readOnly=true) public AppointmentView get(Long id){ AppointmentRequest a=find(id); authorizeRead(a); return toView(a); }
    @Transactional(readOnly=true) public AppointmentPageView list(int page,int size){ LoginUser u=SecurityUtils.requireLoginUser(); Pageable pageable=PageRequest.of(page,size); Page<AppointmentRequest> p; if(isPlatform(u)) p=appointmentRepository.findAllByOrderByCreatedTimeDesc(pageable); else { var ownedShop=shopRepository.findByMerchantUserId(u.getId()); p=ownedShop.map(shop->appointmentRepository.findByUserIdOrShopIdOrderByCreatedTimeDesc(u.getId(),shop.getId(),pageable)).orElseGet(()->appointmentRepository.findByUserIdOrderByCreatedTimeDesc(u.getId(),pageable)); } return new AppointmentPageView(p.getContent().stream().map(this::toView).toList(),p.getTotalElements(),page,size); }
    @Transactional public AppointmentView cancel(Long id,CancelAppointmentRequest r){ AppointmentRequest a=find(id); LoginUser u=SecurityUtils.requireLoginUser(); if(!a.getUserId().equals(u.getId()))throw new BusinessException("403","无权取消该预约"); if(!OrderConstants.APPOINTMENT_PENDING.equals(a.getStatus())&&!OrderConstants.APPOINTMENT_CONTACTING.equals(a.getStatus()))throw new BusinessException("409","当前状态不可取消"); String old=a.getStatus();a.setStatus(OrderConstants.APPOINTMENT_CANCELLED);a.setCancelReason(r==null?null:r.reason());a.setUpdatedTime(LocalDateTime.now(ZoneOffset.UTC));appointmentRepository.save(a);record(a,old,a.getStatus(),u.getId(),a.getCancelReason(),null);return get(a.getId()); }
    @Transactional public AppointmentView updateStatus(Long id, UpdateAppointmentStatusRequest r){ LoginUser u=SecurityUtils.requireLoginUser(); AppointmentRequest a=find(id); boolean owner=shopRepository.findById(a.getShopId()).map(s->s.getMerchantUserId().equals(u.getId())).orElse(false); if(!isPlatform(u) && !owner) throw new BusinessException("403","无权处理该预约"); String target=r.status().trim().toUpperCase(); String old=a.getStatus(); boolean valid=(OrderConstants.APPOINTMENT_PENDING.equals(old)&&OrderConstants.APPOINTMENT_CONTACTING.equals(target))||(OrderConstants.APPOINTMENT_CONTACTING.equals(old)&&OrderConstants.APPOINTMENT_WORKER_ARRANGED.equals(target))||(OrderConstants.APPOINTMENT_WORKER_ARRANGED.equals(old)&&OrderConstants.APPOINTMENT_CLOSED.equals(target)); if(!valid) throw new BusinessException("409","预约状态迁移不合法"); a.setPlatformOperatorId(isPlatform(u) ? u.getId() : null); a.setPlatformNote(r.platformNote()); transition(a,target,u.getId(),r.platformNote()); return toView(a); }
    AppointmentRequest find(Long id){return appointmentRepository.findById(id).orElseThrow(()->new BusinessException("404","预约不存在"));}
    void transition(AppointmentRequest a,String target,Long op,String reason){String old=a.getStatus();a.setStatus(target);a.setUpdatedTime(LocalDateTime.now(ZoneOffset.UTC));appointmentRepository.save(a);record(a,old,target,op,reason,null);}
    private void authorizeRead(AppointmentRequest a){LoginUser u=SecurityUtils.requireLoginUser();if(a.getUserId().equals(u.getId())||isPlatform(u)||shopRepository.findById(a.getShopId()).map(s->s.getMerchantUserId().equals(u.getId())).orElse(false))return;throw new BusinessException("403","无权查看该预约");}
    private boolean isPlatform(LoginUser u){return u.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_username_super_admin")||a.getAuthority().equals("ROLE_username_sys_admin")||a.getAuthority().equals("ROLE_username_aud_admin"));}
    private void record(AppointmentRequest a,String from,String to,Long op,String reason,String key){AppointmentRequestStatusHistory h=new AppointmentRequestStatusHistory();h.setRequestId(a.getId());h.setFromStatus(from);h.setToStatus(to);h.setOperatorUserId(op);h.setReason(reason);h.setRequestIdempotencyKey(key);h.setCreatedTime(LocalDateTime.now(ZoneOffset.UTC));historyRepository.save(h);}
    private AppointmentView toView(AppointmentRequest a){
        Shop shop = shopRepository.findById(a.getShopId()).orElse(null);
        ServiceListing listing = serviceRepository.findById(a.getServiceId()).orElse(null);
        Address address = addressRepository.findById(a.getAddressId()).orElse(null);
        String title = listing == null ? a.getServiceTitleInput() : listing.getTitle();
        AppointmentServiceView service = listing == null ? null : new AppointmentServiceView(
                listing.getId(), listing.getTitle(), listing.getSummary(), listing.getDescription(),
                listing.getPricingUnit(), listing.getBasePrice(), listing.getDurationMinutes());
        AppointmentAddressView addressView = address == null ? null : new AppointmentAddressView(
                address.getId(), address.getReceiverName(), address.getReceiverPhone(), address.getProvince(),
                address.getCity(), address.getDistrict(), address.getDetail(), address.getLongitude(), address.getLatitude());
        List<AppointmentStatusHistoryView> history = historyRepository.findByRequestIdOrderByCreatedTimeAsc(a.getId()).stream()
                .map(item -> new AppointmentStatusHistoryView(item.getId(), item.getFromStatus(), item.getToStatus(),
                        item.getOperatorUserId(), item.getReason(), item.getCreatedTime()))
                .toList();
        return new AppointmentView(a.getId(), a.getRequestNo(), a.getUserId(), a.getShopId(), a.getServiceId(),
                a.getAddressId(), title, a.getRequirementText(), a.getPreferredStart(), a.getPreferredEnd(),
                a.getContactName(), a.getContactPhone(), a.getStatus(), a.getPlatformOperatorId(),
                a.getPlatformNote(), a.getCancelReason(), a.getCreatedTime(), a.getUpdatedTime(),
                shop == null ? null : shop.getShopName(), service, addressView, history);
    }
}

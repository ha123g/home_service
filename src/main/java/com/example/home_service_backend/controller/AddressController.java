package com.example.home_service_backend.controller;

import com.example.home_service_backend.common.result.*;
import com.example.home_service_backend.dto.request.address.CreateAddressRequest;
import com.example.home_service_backend.entity.Address;
import com.example.home_service_backend.repository.AddressRepository;
import com.example.home_service_backend.common.utils.SecurityUtils;
import com.example.home_service_backend.vo.address.AddressView;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
 private final AddressRepository repository;
 public AddressController(AddressRepository repository){this.repository=repository;}
 @PostMapping public ResponseEntity<ResultData<AddressView>> create(@Valid @RequestBody CreateAddressRequest request){
  Address a=new Address(); a.setUserId(SecurityUtils.requireLoginUser().getId()); a.setReceiverName(request.receiverName()); a.setReceiverPhone(request.receiverPhone()); a.setProvince(request.province()); a.setCity(request.city()); a.setDistrict(request.district()); a.setDetail(request.detail()); a.setLongitude(request.longitude()); a.setLatitude(request.latitude()); a.setStatus("ACTIVE"); a.setIsDefault((byte)0); a.setCreatedAt(LocalDateTime.now()); a.setUpdatedAt(LocalDateTime.now()); a=repository.save(a); return ResponseEntity.status(201).body(ResultFactory.buildSuccessData(toView(a))); }
 private AddressView toView(Address a){return new AddressView(a.getId(),a.getReceiverName(),a.getReceiverPhone(),a.getProvince(),a.getCity(),a.getDistrict(),a.getDetail(),a.getLongitude(),a.getLatitude());}
}

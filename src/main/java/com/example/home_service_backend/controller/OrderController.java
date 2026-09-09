package com.example.home_service_backend.controller;

import com.example.home_service_backend.common.constants.ApiConstants;
import com.example.home_service_backend.common.result.ResultData;
import com.example.home_service_backend.common.result.ResultFactory;
import com.example.home_service_backend.dto.request.order.*;
import com.example.home_service_backend.service.OrderService;
import com.example.home_service_backend.vo.order.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.ORDERS_PREFIX)
@Validated
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }
    @PostMapping("/from-appointment")
    public ResponseEntity<ResultData<OrderView>> create(@Valid @RequestBody CreateOrderRequest request) { return ResponseEntity.status(201).body(ResultFactory.buildSuccessData(orderService.createFromAppointment(request))); }
    @PutMapping("/{id}/publish")
    public ResponseEntity<ResultData<OrderView>> publish(@PathVariable Long id, @Valid @RequestBody(required=false) PublishOrderRequest request) { return ResponseEntity.ok(ResultFactory.buildSuccessData(orderService.publish(id, request))); }
    @PostMapping("/{id}/pay")
    public ResponseEntity<ResultData<PaymentView>> pay(@PathVariable Long id, @Valid @RequestBody PayOrderRequest request) { return ResponseEntity.ok(ResultFactory.buildSuccessData(orderService.pay(id, request))); }
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ResultData<OrderView>> confirm(@PathVariable Long id) { return ResponseEntity.ok(ResultFactory.buildSuccessData(orderService.confirm(id))); }
    @PutMapping("/{id}/status")
    public ResponseEntity<ResultData<OrderView>> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) { return ResponseEntity.ok(ResultFactory.buildSuccessData(orderService.updateStatus(id, request))); }
    @GetMapping
    public ResponseEntity<ResultData<OrderPageView>> list(@RequestParam(defaultValue="0") @Min(0) int page, @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) { return ResponseEntity.ok(ResultFactory.buildSuccessData(orderService.list(page, size))); }
    @GetMapping("/{id}")
    public ResponseEntity<ResultData<OrderView>> get(@PathVariable Long id) { return ResponseEntity.ok(ResultFactory.buildSuccessData(orderService.get(id))); }
}

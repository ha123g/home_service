package com.example.home_service_backend.controller;

import com.example.home_service_backend.common.constants.ApiConstants;
import com.example.home_service_backend.common.result.ResultData;
import com.example.home_service_backend.common.result.ResultFactory;
import com.example.home_service_backend.dto.request.appointment.CancelAppointmentRequest;
import com.example.home_service_backend.dto.request.appointment.CreateAppointmentRequest;
import com.example.home_service_backend.dto.request.appointment.UpdateAppointmentStatusRequest;
import com.example.home_service_backend.service.AppointmentService;
import com.example.home_service_backend.vo.appointment.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.APPOINTMENTS_PREFIX)
@Validated
public class AppointmentController {
    private final AppointmentService appointmentService;
    public AppointmentController(AppointmentService appointmentService) { this.appointmentService = appointmentService; }
    @PostMapping
    public ResponseEntity<ResultData<AppointmentView>> create(@Valid @RequestBody CreateAppointmentRequest request) { return ResponseEntity.status(201).body(ResultFactory.buildSuccessData(appointmentService.create(request))); }
    @GetMapping
    public ResponseEntity<ResultData<AppointmentPageView>> list(@RequestParam(defaultValue="0") @Min(0) int page, @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) { return ResponseEntity.ok(ResultFactory.buildSuccessData(appointmentService.list(page, size))); }
    @GetMapping("/{id}")
    public ResponseEntity<ResultData<AppointmentView>> get(@PathVariable Long id) { return ResponseEntity.ok(ResultFactory.buildSuccessData(appointmentService.get(id))); }
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ResultData<AppointmentView>> cancel(@PathVariable Long id, @Valid @RequestBody(required=false) CancelAppointmentRequest request) { return ResponseEntity.ok(ResultFactory.buildSuccessData(appointmentService.cancel(id, request))); }
    @PutMapping("/{id}/status")
    public ResponseEntity<ResultData<AppointmentView>> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateAppointmentStatusRequest request) { return ResponseEntity.ok(ResultFactory.buildSuccessData(appointmentService.updateStatus(id, request))); }
}

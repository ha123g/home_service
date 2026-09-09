package com.example.home_service_backend.controller;

import com.example.home_service_backend.common.result.ResultData;
import com.example.home_service_backend.common.result.ResultFactory;
import com.example.home_service_backend.service.AdminOverviewService;
import com.example.home_service_backend.vo.admin.AdminOverviewView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/overview")
@PreAuthorize("hasAnyRole('username_super_admin','username_sys_admin')")
public class AdminOverviewController {
    private final AdminOverviewService service;

    public AdminOverviewController(AdminOverviewService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ResultData<AdminOverviewView>> overview() {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(service.overview()));
    }
}

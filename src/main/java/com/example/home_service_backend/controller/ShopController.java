package com.example.home_service_backend.controller;

import com.example.home_service_backend.common.result.ResultData;
import com.example.home_service_backend.common.result.ResultFactory;
import com.example.home_service_backend.dto.request.merchant.CreateMerchantApplicationRequest;
import com.example.home_service_backend.dto.request.merchant.ReviewMerchantApplicationRequest;
import com.example.home_service_backend.dto.request.merchant.UpdateShopRequest;
import com.example.home_service_backend.service.ShopService;
import com.example.home_service_backend.vo.merchant.MerchantApplicationPageView;
import com.example.home_service_backend.vo.merchant.MerchantApplicationView;
import com.example.home_service_backend.vo.merchant.ServiceCategoryTreeView;
import com.example.home_service_backend.vo.merchant.ShopPageView;
import com.example.home_service_backend.vo.merchant.ShopView;
import com.example.home_service_backend.vo.merchant.ServiceListingView;
import com.example.home_service_backend.vo.merchant.ServiceCatalogCategoryView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class ShopController {
    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    /**
     * 申请商家
     */
    @PostMapping("/merchant/applications")
    public ResponseEntity<ResultData<MerchantApplicationView>> apply(@Valid @RequestBody CreateMerchantApplicationRequest request) {
        return ResponseEntity.status(201).body(ResultFactory.buildSuccessData(shopService.submitApplication(request)));
    }
    /**
     * 我的申请
     */
    @GetMapping("/merchant/applications/me")
    public ResponseEntity<ResultData<MerchantApplicationView>> myApplication() {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(shopService.myApplication()));
    }
    /**
     * 商家申请列表
     */
    @GetMapping("/merchant/applications")
    @PreAuthorize("hasAnyRole('username_aud_admin','username_sys_admin','username_super_admin')")
    public ResponseEntity<ResultData<MerchantApplicationPageView>> applications(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(shopService.listApplications(status, page, size)));
    }
    /**
     * 商家申请详情
     */
    @GetMapping("/merchant/applications/{id}")
    @PreAuthorize("hasAnyRole('username_aud_admin','username_sys_admin','username_super_admin')")
    public ResponseEntity<ResultData<MerchantApplicationView>> applicationDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(shopService.getApplication(id)));
    }

    /**
     * 取消我的申请
     */
    @PutMapping("/merchant/applications/{id}/cancel")
    public ResponseEntity<ResultData<MerchantApplicationView>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(shopService.cancelMyApplication(id)));
    }

    /**
     * 审查商家申请
     */
    @PutMapping("/merchant/applications/{id}/review")
    @PreAuthorize("hasAnyRole('username_aud_admin','username_super_admin')")
    public ResponseEntity<ResultData<MerchantApplicationView>> review(
            @PathVariable Long id, @Valid @RequestBody ReviewMerchantApplicationRequest request) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(shopService.review(id, request)));
    }
    /**
     * 商家列表
     */
    @GetMapping("/shops")
    public ResponseEntity<ResultData<ShopPageView>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(shopService.search(keyword, province, city, district,
                status, categoryId, latitude, longitude, radiusKm, page, size)));
    }

    /**
     * 商家详情
     */
    @GetMapping("/shops/{id}")
    public ResponseEntity<ResultData<ShopView>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(shopService.getShop(id)));
    }

    @GetMapping("/shops/{id}/services")
    public ResponseEntity<ResultData<List<ServiceListingView>>> services(@PathVariable Long id) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(shopService.listOnlineServices(id)));
    }
    /**
     * 商家分类树
     */
    @GetMapping("/service-categories/tree")
    public ResponseEntity<ResultData<List<ServiceCategoryTreeView>>> categories() {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(shopService.categoryTree()));
    }

    @GetMapping("/service-listings/catalog")
    public ResponseEntity<ResultData<List<ServiceListingView>>> serviceCatalog() {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(shopService.listServiceCatalog()));
    }

    @GetMapping("/service-listings/catalog/tree")
    public ResponseEntity<ResultData<List<ServiceCatalogCategoryView>>> serviceCatalogTree() {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(shopService.serviceCatalogTree()));
    }

    /**
     * 更新商家
     */
    @PutMapping("/shops/{id}")
    @PreAuthorize("hasAnyRole('username_pla_user','username_sys_admin','username_super_admin')")
    public ResponseEntity<ResultData<ShopView>> update(@PathVariable Long id, @Valid @RequestBody UpdateShopRequest request) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(shopService.updateShop(id, request)));
    }

    /**
     * 更新商家状态
     */
    @PutMapping("/shops/{id}/status")
    @PreAuthorize("hasAnyRole('username_pla_user','username_sys_admin','username_super_admin')")
    public ResponseEntity<ResultData<ShopView>> changeStatus(@PathVariable Long id, @RequestParam String value) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(shopService.changeStatus(id, value)));
    }
}

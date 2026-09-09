package com.example.home_service_backend.controller;

import com.example.home_service_backend.common.constants.ApiConstants;
import com.example.home_service_backend.common.result.ResultData;
import com.example.home_service_backend.common.result.ResultFactory;
import com.example.home_service_backend.service.AmapGeocodingService;
import com.example.home_service_backend.vo.map.GeocodeView;
import com.example.home_service_backend.vo.map.MapWebConfigView;
import com.example.home_service_backend.config.AmapProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping(ApiConstants.MAP_PREFIX)
@Validated
public class MapController {
    private final AmapGeocodingService geocodingService;
    private final AmapProperties amapProperties;

    public MapController(AmapGeocodingService geocodingService, AmapProperties amapProperties) {
        this.geocodingService = geocodingService;
        this.amapProperties = amapProperties;
    }

    /** 浏览器加载高德 JS API 所需的公开 Key 与安全码。 */
    @GetMapping("/web-config")
    public ResponseEntity<ResultData<MapWebConfigView>> webConfig() {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(
                new MapWebConfigView(amapProperties.webApiKey(), amapProperties.securityKey())));
    }

    /**
     * 地理编码
     */
    @GetMapping("/geocode")
    public ResponseEntity<ResultData<GeocodeView>> geocode(
            @RequestParam @NotBlank @Size(max = 255) String address,
            @RequestParam(required = false) @Size(max = 64) String city) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(geocodingService.geocode(address, city)));
    }

    /**
     * 逆地理编码
     */
    @GetMapping("/regeocode")
    public ResponseEntity<ResultData<GeocodeView>> reverseGeocode(
            @RequestParam @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
            @RequestParam @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(
                geocodingService.reverseGeocode(longitude, latitude)));
    }
}

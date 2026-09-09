package com.example.home_service_backend.service;

import com.example.home_service_backend.common.exception.BusinessException;
import com.example.home_service_backend.config.AmapProperties;
import com.example.home_service_backend.vo.map.GeocodeView;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class AmapGeocodingService {
    private final RestClient restClient;
    private final AmapProperties properties;

    public AmapGeocodingService(RestClient amapRestClient, AmapProperties properties) {
        this.restClient = amapRestClient;
        this.properties = properties;
    }

    @SuppressWarnings("unchecked")
    public GeocodeView geocode(String address, String city) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new BusinessException("503", "高德地图 Key 尚未配置");
        }
        int attempts = Math.max(1, Math.min(3, properties.maxRetries() + 1));
        for (int attempt = 1; attempt <= attempts; attempt++) try {
            Map<String, Object> body = restClient.get().uri(builder -> {
                builder.path(properties.geocodePath()).queryParam("key", properties.apiKey())
                        .queryParam("address", address).queryParam("output", "JSON");
                if (city != null && !city.isBlank()) builder.queryParam("city", city);
                return builder.build();
            }).retrieve().body(Map.class);
            if (body == null || !"1".equals(String.valueOf(body.get("status")))) {
                throw amapError(body, "地址解析");
            }
            List<Map<String, Object>> geocodes = (List<Map<String, Object>>) body.get("geocodes");
            if (geocodes == null || geocodes.isEmpty()) throw new BusinessException("422", "未找到对应地址");
            Map<String, Object> item = geocodes.getFirst();
            String[] location = String.valueOf(item.get("location")).split(",");
            if (location.length != 2) throw new BusinessException("502", "地图服务返回了无效坐标");
            String province = text(item, "province");
            String resolvedCity = text(item, "city");
            // 高德对北京、上海等直辖市通常返回 city=[]，表单仍需要城市字段。
            if (resolvedCity == null || resolvedCity.isBlank()) resolvedCity = province;
            return new GeocodeView(text(item, "formatted_address"), province,
                    resolvedCity, text(item, "district"), text(item, "adcode"),
                    new BigDecimal(location[0]), new BigDecimal(location[1]));
        } catch (BusinessException ex) {
            throw ex;
        } catch (NumberFormatException ex) {
            throw new BusinessException("502", "地图服务返回了无效坐标");
        } catch (RestClientException ex) {
            // GET 地理编码是幂等调用，按配置进行有限重试；不得记录包含 Key 的完整 URL。
            backoff(attempt, attempts);
        }
        throw new BusinessException("502", "高德地图服务暂时不可用");
    }

    @SuppressWarnings("unchecked")
    public GeocodeView reverseGeocode(BigDecimal longitude, BigDecimal latitude) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new BusinessException("503", "高德地图 Key 尚未配置");
        }
        int attempts = Math.max(1, Math.min(3, properties.maxRetries() + 1));
        for (int attempt = 1; attempt <= attempts; attempt++) try {
            Map<String, Object> body = restClient.get().uri(builder -> builder.path(properties.regeocodePath())
                    .queryParam("key", properties.apiKey())
                    .queryParam("location", longitude.toPlainString() + "," + latitude.toPlainString())
                    .queryParam("extensions", "base").queryParam("output", "JSON").build())
                    .retrieve().body(Map.class);
            if (body == null || !"1".equals(String.valueOf(body.get("status")))) {
                throw amapError(body, "逆地理编码");
            }
            Map<String, Object> regeocode = (Map<String, Object>) body.get("regeocode");
            if (regeocode == null || regeocode.isEmpty()) throw new BusinessException("422", "未找到坐标对应地址");
            Map<String, Object> component = (Map<String, Object>) regeocode.get("addressComponent");
            if (component == null) component = Map.of();
            String province = text(component, "province");
            String city = text(component, "city");
            if (city == null || city.isBlank()) city = province;
            return new GeocodeView(text(regeocode, "formatted_address"), province,
                    city, text(component, "district"), text(component, "adcode"),
                    longitude, latitude);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RestClientException ex) {
            // 仅对幂等 GET 做有限重试。
            backoff(attempt, attempts);
        }
        throw new BusinessException("502", "高德地图服务暂时不可用");
    }

    private String text(Map<String, Object> item, String key) {
        Object value = item.get(key);
        if (value instanceof List<?> values) return values.isEmpty() ? null : String.valueOf(values.getFirst());
        return value == null ? null : String.valueOf(value);
    }

    private void backoff(int attempt, int attempts) {
        if (attempt >= attempts) return;
        try {
            Thread.sleep(Math.min(250L * attempt, 500L));
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private BusinessException amapError(Map<String, Object> body, String operation) {
        String info = body == null ? null : String.valueOf(body.get("info"));
        String code = body == null ? null : String.valueOf(body.get("infocode"));
        if (java.util.Set.of("10001", "10002", "10003", "10004", "10008", "10009", "10010")
                .contains(code)
                || "INVALID_USER_KEY".equalsIgnoreCase(info)
                || "USERKEY_PLAT_NOMATCH".equalsIgnoreCase(info)
                || "USERKEY_SERVICE_PLAT_NOMATCH".equalsIgnoreCase(info)) {
            return new BusinessException("502", "高德地图 Key 无效或未开通 Web 服务，请联系管理员配置有效 Key");
        }
        if ("10044".equals(code) || "DAILY_QUERY_OVER_LIMIT".equals(info)) {
            return new BusinessException("502", "高德地图今日调用次数已用尽，请稍后重试");
        }
        return new BusinessException("502", "高德地图" + operation + "失败，请稍后重试");
    }
}

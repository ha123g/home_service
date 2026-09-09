package com.example.home_service_backend.vo.map;

/** 高德 JS API 的浏览器端公开配置，不包含服务端或其他供应商密钥。 */
public record MapWebConfigView(String apiKey, String securityCode) {
}

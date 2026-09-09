package com.example.home_service_backend.service;

import com.example.home_service_backend.common.exception.BusinessException;
import com.example.home_service_backend.config.StripeProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Stripe Test mode 支付网关；只允许使用测试密钥，不触碰生产支付。 */
@Service
public class StripeSandboxPaymentGateway {
    private final StripeProperties properties;
    private final RestClient client;

    public StripeSandboxPaymentGateway(StripeProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.readTimeout());
        this.client = restClientBuilder.baseUrl(properties.apiBaseUrl()).requestFactory(factory).build();
    }

    public PaymentSession create(BigDecimal amount, String orderNo, String idempotencyKey) {
        if (!properties.enabled() || !properties.sandbox()
                || properties.secretKey() == null || properties.secretKey().isBlank()
                || !properties.secretKey().startsWith("sk_test_")) {
            throw new BusinessException("503", "Stripe 沙盒支付未配置，请填写 STRIPE_SECRET_KEY");
        }

        Map<String, String> form = new LinkedHashMap<>();
        form.put("amount", amount.setScale(2).movePointRight(2).toBigIntegerExact().toString());
        form.put("currency", "cny");
        // Stripe 官方测试支付方式，会在 Test mode 中返回 succeeded，不会产生真实扣款。
        form.put("payment_method", "pm_card_visa");
        form.put("confirm", "true");
        form.put("description", "家政服务订单 " + orderNo);
        form.put("metadata[order_no]", orderNo);
        RuntimeException lastFailure = null;
        for (int attempt = 0; attempt <= properties.maxRetries(); attempt++) {
            try {
                JsonNode response = client.post()
                        .uri("/v1/payment_intents")
                        .headers(headers -> {
                            headers.setBearerAuth(properties.secretKey());
                            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                            headers.set("Idempotency-Key", idempotencyKey);
                        })
                        .body(formEncode(form))
                        .retrieve()
                        .body(JsonNode.class);
                if (response == null || response.path("id").asText().isBlank()) {
                    throw new BusinessException("502", "Stripe 沙盒支付返回结果为空");
                }
                return new PaymentSession(response.path("id").asText(), null,
                        response.path("status").asText());
            } catch (BusinessException exception) {
                throw exception;
            } catch (RestClientResponseException exception) {
                lastFailure = exception;
                int status = exception.getStatusCode().value();
                if (status < 500 && status != 429) {
                    throw new BusinessException("502", "Stripe 沙盒支付失败，请检查沙盒配置或稍后重试", exception);
                }
            } catch (RestClientException exception) {
                lastFailure = exception;
            }
        }
        throw new BusinessException("502", "Stripe 沙盒支付服务暂时不可用，请稍后重试", lastFailure);
    }

    private String formEncode(Map<String, String> values) {
        return values.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record PaymentSession(String providerPaymentId, String checkoutUrl, String status) {}
}

package com.example.home_service_backend.agent.common.utils;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/** 在用户文本进入路由、Chat 或 Embedding 模型前执行最小化脱敏。 */
@Component
public class AgentInputSanitizerUtil {
    private static final Pattern SECRET_ASSIGNMENT = Pattern.compile(
            "(?i)(密码|口令|token|api[-_ ]?key|secret)(\\s*(?:是|为|=|:|：)\\s*)[^\\s，。；,;]{3,}");
    private static final Pattern PHONE = Pattern.compile("(?<!\\d)1[3-9]\\d{9}(?!\\d)");
    private static final Pattern ID_CARD = Pattern.compile("(?<!\\d)\\d{15}(?:\\d{2}[0-9Xx])?(?!\\d)");
    private static final Pattern BANK_CARD = Pattern.compile("(?<!\\d)\\d{16,19}(?!\\d)");
    private static final Pattern EMAIL = Pattern.compile(
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern ADDRESS_ASSIGNMENT = Pattern.compile(
            "(地址|住址)(\\s*(?:是|为|=|:|：)\\s*)[^，。；,;]{3,}");

    public String sanitizeForModel(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String value = SECRET_ASSIGNMENT.matcher(input).replaceAll("$1$2[REDACTED_SECRET]");
        value = PHONE.matcher(value).replaceAll("[REDACTED_PHONE]");
        value = ID_CARD.matcher(value).replaceAll("[REDACTED_ID]");
        value = BANK_CARD.matcher(value).replaceAll("[REDACTED_PAYMENT]");
        value = EMAIL.matcher(value).replaceAll("[REDACTED_EMAIL]");
        return ADDRESS_ASSIGNMENT.matcher(value).replaceAll("$1$2[REDACTED_ADDRESS]");
    }
}

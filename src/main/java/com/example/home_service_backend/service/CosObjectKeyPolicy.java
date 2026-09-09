package com.example.home_service_backend.service;

import com.example.home_service_backend.common.constants.ApiConstants;
import com.example.home_service_backend.common.constants.FileStorageConstants;
import com.example.home_service_backend.common.enums.FileUploadPurpose;
import com.example.home_service_backend.common.exception.BusinessException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验并解析后端签发的 COS Object Key。拒绝 Base64、公网 URL 和路径穿越。
 */
public final class CosObjectKeyPolicy {
    private static final Pattern KEY_PATTERN = Pattern.compile(
            "^u/([1-9][0-9]{0,18})/(avatar|merchant-logo|merchant-gallery|merchant-document)"
                    + "/(\\d{8})/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})"
                    + "\\.(jpg|jpeg|png|webp)$",
            Pattern.CASE_INSENSITIVE);

    private CosObjectKeyPolicy() {
    }

    public record ParsedKey(long userId, FileUploadPurpose purpose, String objectKey) {
    }

    public static String previewPath(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        parse(objectKey);
        return FileStorageConstants.PREVIEW_PATH + "?objectKey="
                + java.net.URLEncoder.encode(objectKey.trim(), StandardCharsets.UTF_8);
    }

    public static String normalizeSubmitted(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("data:") || lower.contains("base64,")) {
            throw new BusinessException("400", "图片必须先上传至对象存储，不能使用 Base64 作为地址");
        }
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            throw new BusinessException("400", "不能提交外部图片地址，请使用平台对象 Key");
        }
        if (trimmed.startsWith(FileStorageConstants.PREVIEW_PATH)
                || trimmed.startsWith(ApiConstants.FILES_PREFIX + "/preview")) {
            return extractQueryObjectKey(trimmed);
        }
        if (trimmed.contains("://") || trimmed.startsWith("/") || trimmed.contains("..")) {
            throw new BusinessException("400", "COS Object Key 不合法");
        }
        return parse(trimmed).objectKey();
    }

    public static ParsedKey parse(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new BusinessException("400", "COS Object Key 不能为空");
        }
        String value = objectKey.trim();
        Matcher matcher = KEY_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new BusinessException("400", "COS Object Key 不合法");
        }
        long userId;
        try {
            userId = Long.parseLong(matcher.group(1));
        } catch (NumberFormatException ex) {
            throw new BusinessException("400", "COS Object Key 不合法");
        }
        FileUploadPurpose purpose = switch (matcher.group(2).toLowerCase(Locale.ROOT)) {
            case "avatar" -> FileUploadPurpose.AVATAR;
            case "merchant-logo" -> FileUploadPurpose.MERCHANT_LOGO;
            case "merchant-gallery" -> FileUploadPurpose.MERCHANT_GALLERY;
            case "merchant-document" -> FileUploadPurpose.MERCHANT_DOCUMENT;
            default -> throw new BusinessException("400", "COS Object Key 不合法");
        };
        return new ParsedKey(userId, purpose, value);
    }

    public static ParsedKey requireOwned(String objectKey, long userId, Set<FileUploadPurpose> allowed) {
        ParsedKey parsed = parse(objectKey);
        if (parsed.userId() != userId) {
            throw new BusinessException("403", "不能使用他人名下的上传对象");
        }
        if (allowed != null && !allowed.isEmpty() && !allowed.contains(parsed.purpose())) {
            throw new BusinessException("400", "图片用途与对象 Key 不匹配");
        }
        return parsed;
    }

    public static ParsedKey requireReadable(String objectKey, long userId, boolean admin) {
        ParsedKey parsed = parse(objectKey);
        if (parsed.purpose() == FileUploadPurpose.AVATAR && parsed.userId() != userId && !admin) {
            throw new BusinessException("403", "无权预览该头像");
        }
        return parsed;
    }

    private static String extractQueryObjectKey(String url) {
        int queryIndex = url.indexOf('?');
        if (queryIndex < 0) {
            throw new BusinessException("400", "COS Object Key 不合法");
        }
        String query = url.substring(queryIndex + 1);
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String name = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
            if ("objectKey".equals(name)) {
                return parse(URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8)).objectKey();
            }
        }
        throw new BusinessException("400", "COS Object Key 不合法");
    }
}

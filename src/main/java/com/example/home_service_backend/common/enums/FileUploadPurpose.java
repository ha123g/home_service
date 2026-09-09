package com.example.home_service_backend.common.enums;

import java.util.Locale;

/**
 * 受控 COS 上传用途。路径段写入 Object Key，不得把密钥或临时 URL 写入数据库。
 */
public enum FileUploadPurpose {
    AVATAR("avatar"),
    MERCHANT_LOGO("merchant-logo"),
    MERCHANT_GALLERY("merchant-gallery"),
    MERCHANT_DOCUMENT("merchant-document");

    private final String keySegment;

    FileUploadPurpose(String keySegment) {
        this.keySegment = keySegment;
    }

    public String keySegment() {
        return keySegment;
    }

    public static FileUploadPurpose fromParam(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("上传用途不能为空");
        }
        try {
            return FileUploadPurpose.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("上传用途不合法");
        }
    }

    public static FileUploadPurpose fromImageType(String imageType) {
        if (imageType == null) {
            return MERCHANT_GALLERY;
        }
        return switch (imageType.trim().toUpperCase(Locale.ROOT)) {
            case "LOGO" -> MERCHANT_LOGO;
            case "QUALIFICATION", "ID_FRONT", "ID_BACK" -> MERCHANT_DOCUMENT;
            default -> MERCHANT_GALLERY;
        };
    }
}

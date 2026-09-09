package com.example.home_service_backend.common.constants;

import java.util.Set;

public final class FileStorageConstants {
    public static final String PREVIEW_PATH = ApiConstants.FILES_PREFIX + "/preview";
    public static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    public static final int MAX_MERCHANT_IMAGES = 12;
    public static final int MAX_MERCHANT_GALLERY = 8;
    public static final int MAX_IMAGE_EDGE = 8000;

    private FileStorageConstants() {
    }
}

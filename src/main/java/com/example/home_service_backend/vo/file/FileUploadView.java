package com.example.home_service_backend.vo.file;

public record FileUploadView(String objectKey, String purpose, String mimeType, long fileSize,
                             String sha256, String previewUrl) {
}

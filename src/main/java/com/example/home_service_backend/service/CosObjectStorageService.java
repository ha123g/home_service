package com.example.home_service_backend.service;

import com.example.home_service_backend.common.enums.FileUploadPurpose;
import com.example.home_service_backend.common.exception.BusinessException;
import com.example.home_service_backend.config.CosProperties;
import com.example.home_service_backend.vo.file.FileUploadView;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

/**
 * 腾讯云 COS 基础设施：上传与短期预览签名。调用必须在数据库事务外。
 */
@Service
public class CosObjectStorageService {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private final CosProperties properties;
    private final ObjectProvider<COSClient> cosClient;

    public CosObjectStorageService(CosProperties properties, ObjectProvider<COSClient> cosClient) {
        this.properties = properties;
        this.cosClient = cosClient;
    }

    public FileUploadView upload(MultipartFile file, FileUploadPurpose purpose, long userId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("400", "请选择要上传的图片");
        }
        if (file.getSize() > properties.maxFileSize()) {
            throw new BusinessException("400", "图片大小不能超过 " + (properties.maxFileSize() / 1024 / 1024) + "MB");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new BusinessException("400", "读取上传文件失败", ex);
        }
        if (bytes.length > properties.maxFileSize()) {
            throw new BusinessException("400", "图片大小超出限制");
        }
        ImageContentInspector.InspectedImage inspected = ImageContentInspector.inspect(bytes, file.getOriginalFilename());
        String objectKey = "u/" + userId + "/" + purpose.keySegment() + "/" + DAY.format(Instant.now())
                + "/" + UUID.randomUUID() + "." + inspected.extension();
        String sha256 = sha256(bytes);
        putObject(objectKey, bytes, inspected.mimeType());
        return new FileUploadView(objectKey, purpose.name(), inspected.mimeType(), bytes.length, sha256,
                CosObjectKeyPolicy.previewPath(objectKey));
    }

    public String previewPath(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        try {
            return CosObjectKeyPolicy.previewPath(objectKey);
        } catch (BusinessException ex) {
            return null;
        }
    }

    public String signedGetUrl(String objectKey) {
        COSClient client = requireClient();
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                properties.bucket(), objectKey, HttpMethodName.GET);
        request.setExpiration(Date.from(Instant.now().plus(properties.previewTtl())));
        try {
            return client.generatePresignedUrl(request).toString();
        } catch (CosClientException ex) {
            throw new BusinessException("502", "对象存储暂时不可用", ex);
        }
    }

    public CosObjectKeyPolicy.ParsedKey requireOwnedKey(String objectKey, long userId, Set<FileUploadPurpose> allowed) {
        String normalized = CosObjectKeyPolicy.normalizeSubmitted(objectKey);
        if (normalized == null) {
            return null;
        }
        return CosObjectKeyPolicy.requireOwned(normalized, userId, allowed);
    }

    public CosObjectKeyPolicy.ParsedKey requireReadable(String objectKey, long userId, boolean admin) {
        return CosObjectKeyPolicy.requireReadable(objectKey, userId, admin);
    }

    private void putObject(String objectKey, byte[] bytes, String mimeType) {
        COSClient client = requireClient();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType(mimeType);
        metadata.setCacheControl("private, max-age=0");
        int attempts = Math.max(1, properties.maxRetries() + 1);
        CosClientException last = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
                client.putObject(new PutObjectRequest(properties.bucket(), objectKey, input, metadata));
                return;
            } catch (CosServiceException ex) {
                throw new BusinessException("502", "对象存储写入失败", ex);
            } catch (IOException ex) {
                throw new BusinessException("502", "对象存储写入失败", ex);
            } catch (CosClientException ex) {
                last = ex;
            }
        }
        throw new BusinessException("502", "对象存储暂时不可用", last);
    }

    private COSClient requireClient() {
        if (!properties.configured()) {
            throw new BusinessException("503", "腾讯云 COS 尚未配置");
        }
        COSClient client = cosClient.getIfAvailable();
        if (client == null) {
            throw new BusinessException("503", "腾讯云 COS 尚未配置");
        }
        return client;
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new BusinessException("500", "无法计算文件摘要", ex);
        }
    }
}

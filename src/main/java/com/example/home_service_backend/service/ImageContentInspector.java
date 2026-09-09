package com.example.home_service_backend.service;

import com.example.home_service_backend.common.constants.FileStorageConstants;
import com.example.home_service_backend.common.exception.BusinessException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 按魔数、扩展名和像素边界校验上传图片，拒绝 SVG/HTML 等可执行内容。
 */
public final class ImageContentInspector {
    public record InspectedImage(String mimeType, String extension) {
    }

    private ImageContentInspector() {
    }

    public static InspectedImage inspect(byte[] bytes, String originalFilename) {
        if (bytes == null || bytes.length < 12) {
            throw new BusinessException("400", "图片内容不完整");
        }
        rejectMarkup(bytes);
        InspectedImage detected = detect(bytes);
        String filename = originalFilename == null ? "" : originalFilename.trim().toLowerCase(Locale.ROOT);
        if (!filename.isEmpty() && filename.contains(".")) {
            String ext = filename.substring(filename.lastIndexOf('.') + 1);
            if (!"jpg".equals(ext) && !"jpeg".equals(ext) && !"png".equals(ext) && !"webp".equals(ext)) {
                throw new BusinessException("400", "仅支持 JPG、PNG 或 WebP 图片");
            }
            if ("png".equals(ext) && !"image/png".equals(detected.mimeType())) {
                throw new BusinessException("400", "文件扩展名与图片内容不一致");
            }
            if (("jpg".equals(ext) || "jpeg".equals(ext)) && !"image/jpeg".equals(detected.mimeType())) {
                throw new BusinessException("400", "文件扩展名与图片内容不一致");
            }
            if ("webp".equals(ext) && !"image/webp".equals(detected.mimeType())) {
                throw new BusinessException("400", "文件扩展名与图片内容不一致");
            }
        }
        if ("image/jpeg".equals(detected.mimeType()) || "image/png".equals(detected.mimeType())) {
            assertBoundedRaster(bytes);
        }
        return detected;
    }

    private static InspectedImage detect(byte[] bytes) {
        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) {
            return new InspectedImage("image/jpeg", "jpg");
        }
        if (bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return new InspectedImage("image/png", "png");
        }
        if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return new InspectedImage("image/webp", "webp");
        }
        throw new BusinessException("400", "仅支持 JPG、PNG 或 WebP 图片");
    }

    private static void rejectMarkup(byte[] bytes) {
        int length = Math.min(bytes.length, 256);
        String head = new String(bytes, 0, length, StandardCharsets.US_ASCII).toLowerCase(Locale.ROOT);
        if (head.contains("<svg") || head.contains("<html") || head.contains("<!doctype") || head.contains("<?xml")) {
            throw new BusinessException("400", "不支持该图片内容");
        }
    }

    private static void assertBoundedRaster(byte[] bytes) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                throw new BusinessException("400", "无法解析图片内容");
            }
            if (image.getWidth() > FileStorageConstants.MAX_IMAGE_EDGE
                    || image.getHeight() > FileStorageConstants.MAX_IMAGE_EDGE) {
                throw new BusinessException("400", "图片尺寸超出限制");
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new BusinessException("400", "无法解析图片内容", ex);
        }
    }
}

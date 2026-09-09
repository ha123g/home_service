package com.example.home_service_backend.service;

import com.example.home_service_backend.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageContentInspectorTests {
    @Test
    void detectPngMagicBytes() {
        byte[] png = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0, 0, 0, 0, 0};
        BusinessException exception = assertThrows(BusinessException.class,
                () -> ImageContentInspector.inspect(png, "logo.png"));
        // PNG header without IHDR cannot be decoded by ImageIO.
        assertEquals("400", exception.getCode());
    }

    @Test
    void rejectSvgMarkup() {
        byte[] svg = "<svg xmlns='http://www.w3.org/2000/svg'></svg>".getBytes(StandardCharsets.US_ASCII);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> ImageContentInspector.inspect(pad(svg), "evil.svg"));
        assertEquals("400", exception.getCode());
    }

    @Test
    void rejectMismatchedExtension() {
        byte[] jpeg = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        BusinessException exception = assertThrows(BusinessException.class,
                () -> ImageContentInspector.inspect(jpeg, "logo.png"));
        assertEquals("400", exception.getCode());
    }

    private static byte[] pad(byte[] source) {
        byte[] bytes = new byte[Math.max(12, source.length)];
        System.arraycopy(source, 0, bytes, 0, source.length);
        return bytes;
    }
}

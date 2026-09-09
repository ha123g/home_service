package com.example.home_service_backend.service;

import com.example.home_service_backend.common.enums.FileUploadPurpose;
import com.example.home_service_backend.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CosObjectKeyPolicyTests {
    @Test
    void parseOwnedMerchantLogoKey() {
        String key = "u/7/merchant-logo/20260908/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee.jpg";
        CosObjectKeyPolicy.ParsedKey parsed = CosObjectKeyPolicy.requireOwned(
                key, 7L, Set.of(FileUploadPurpose.MERCHANT_LOGO));
        assertEquals(7L, parsed.userId());
        assertEquals(FileUploadPurpose.MERCHANT_LOGO, parsed.purpose());
        assertEquals(key, parsed.objectKey());
    }

    @Test
    void rejectBase64AsObjectKey() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> CosObjectKeyPolicy.normalizeSubmitted("data:image/png;base64,AAAA"));
        assertEquals("400", exception.getCode());
    }

    @Test
    void extractObjectKeyFromBackendPreviewPath() {
        String key = "u/3/avatar/20260908/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee.png";
        String preview = CosObjectKeyPolicy.previewPath(key);
        assertEquals(key, CosObjectKeyPolicy.normalizeSubmitted(preview));
    }

    @Test
    void rejectForeignUserKey() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> CosObjectKeyPolicy.requireOwned(
                        "u/8/avatar/20260908/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee.png",
                        7L, Set.of(FileUploadPurpose.AVATAR)));
        assertEquals("403", exception.getCode());
    }

    @Test
    void rejectExternalHttpUrl() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> CosObjectKeyPolicy.normalizeSubmitted("https://example.com/a.png"));
        assertEquals("400", exception.getCode());
    }
}

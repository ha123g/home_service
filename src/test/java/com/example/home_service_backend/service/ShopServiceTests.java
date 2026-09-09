package com.example.home_service_backend.service;

import com.example.home_service_backend.common.exception.BusinessException;
import com.example.home_service_backend.dto.request.merchant.ReviewMerchantApplicationRequest;
import com.example.home_service_backend.entity.MerchantApplication;
import com.example.home_service_backend.entity.MerchantApplicationLocation;
import com.example.home_service_backend.repository.*;
import com.example.home_service_backend.security.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopServiceTests {
    @Mock ShopRepository shopRepository;
    @Mock MerchantApplicationRepository applicationRepository;
    @Mock MerchantApplicationLocationRepository locationRepository;
    @Mock MerchantApplicationImageRepository imageRepository;
    @Mock UserRepository userRepository;
    @Mock UserRoleRepository userRoleRepository;
    @Mock ServiceCategoryRepository categoryRepository;
    @Mock ServiceListingCategoryRepository listingCategoryRepository;
    @Mock ServiceListingRepository listingRepository;
    @Mock CosObjectStorageService cosObjectStorageService;
    ShopService service;

    @BeforeEach
    void setUp() {
        service = new ShopService(shopRepository, applicationRepository, locationRepository,
                imageRepository, userRepository, userRoleRepository, categoryRepository,
                listingCategoryRepository, listingRepository, cosObjectStorageService);
        LoginUser user = new LoginUser(7L, "user", null, "normal", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void nearbySearchRequiresCompleteCoordinateTuple() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.search(null, null, null, null, null, null,
                        BigDecimal.valueOf(30), null, 5D, 0, 20));
        assertEquals("400", exception.getCode());
    }

    @Test
    void nearbySearchRejectsUnboundedRadius() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.search(null, null, null, null, null, null,
                        BigDecimal.valueOf(30), BigDecimal.valueOf(120), 501D, 0, 20));
        assertEquals("400", exception.getCode());
    }

    @Test
    void ordinaryUserCannotQuerySuspendedShops() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.search(null, null, null, null, "SUSPENDED", null,
                        null, null, null, 0, 20));
        assertEquals("403", exception.getCode());
    }

    @Test
    void rejectionAuditsDecisionWithoutCreatingShop() {
        MerchantApplication application = new MerchantApplication();
        application.setId(9L);
        application.setUserId(7L);
        application.setStatus("PENDING");
        MerchantApplicationLocation location = new MerchantApplicationLocation();
        location.setApplicationId(9L);
        when(applicationRepository.findById(9L)).thenReturn(Optional.of(application));
        when(applicationRepository.saveAndFlush(application)).thenReturn(application);
        when(locationRepository.findByApplicationId(9L)).thenReturn(Optional.of(location));
        when(imageRepository.findByApplicationIdAndStatusOrderById(9L, "UPLOADED")).thenReturn(List.of());

        var view = service.review(9L, new ReviewMerchantApplicationRequest("REJECTED", "资料不完整"));

        assertEquals("REJECTED", view.status());
        assertEquals(7L, view.reviewerId());
        assertEquals("资料不完整", view.reviewRemark());
        verify(shopRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}

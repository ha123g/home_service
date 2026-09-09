package com.example.home_service_backend.service;

import com.example.home_service_backend.repository.AiRequestLogRepository;
import com.example.home_service_backend.repository.MerchantApplicationRepository;
import com.example.home_service_backend.repository.ShopRepository;
import com.example.home_service_backend.repository.UserRepository;
import com.example.home_service_backend.vo.admin.AdminOverviewView;
import org.springframework.stereotype.Service;

@Service
public class AdminOverviewService {
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final MerchantApplicationRepository applicationRepository;
    private final AiRequestLogRepository aiRequestLogRepository;

    public AdminOverviewService(UserRepository userRepository,
                                ShopRepository shopRepository,
                                MerchantApplicationRepository applicationRepository,
                                AiRequestLogRepository aiRequestLogRepository) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.applicationRepository = applicationRepository;
        this.aiRequestLogRepository = aiRequestLogRepository;
    }

    public AdminOverviewView overview() {
        long totalRequests = aiRequestLogRepository.count();
        long failed = aiRequestLogRepository.countByStatus("FAILED");
        return new AdminOverviewView(
                userRepository.count(),
                shopRepository.countByStatus("OPEN"),
                applicationRepository.countByStatus("PENDING"),
                totalRequests,
                0,
                failed);
    }
}

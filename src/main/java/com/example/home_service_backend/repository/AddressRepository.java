package com.example.home_service_backend.repository;

import com.example.home_service_backend.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByIdAndUserIdAndStatus(Long id, Long userId, String status);
}

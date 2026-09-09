package com.example.home_service_backend.dto.request.address;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateAddressRequest(@Size(max=64) String receiverName, @Pattern(regexp="^[0-9+()\\- ]{6,32}$") String receiverPhone, @NotBlank @Size(max=64) String province, @NotBlank @Size(max=64) String city, @Size(max=64) String district, @NotBlank @Size(max=255) String detail, @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude, @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude) {}

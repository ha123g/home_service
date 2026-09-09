package com.example.home_service_backend.dto.request.appointment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record UpdateAppointmentStatusRequest(@NotBlank String status, @Size(max=2048) String platformNote) {}

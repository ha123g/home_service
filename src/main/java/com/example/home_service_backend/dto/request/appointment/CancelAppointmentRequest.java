package com.example.home_service_backend.dto.request.appointment;
import jakarta.validation.constraints.Size;
public record CancelAppointmentRequest(@Size(max = 512) String reason) {}

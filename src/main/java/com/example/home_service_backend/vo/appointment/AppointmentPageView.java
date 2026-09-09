package com.example.home_service_backend.vo.appointment;
import java.util.List;
public record AppointmentPageView(List<AppointmentView> items, long total, int page, int size) {}

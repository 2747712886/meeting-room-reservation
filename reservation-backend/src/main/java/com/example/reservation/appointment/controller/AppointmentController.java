package com.example.reservation.appointment.controller;

import com.example.reservation.appointment.dto.AppointmentCancelRequest;
import com.example.reservation.appointment.dto.AppointmentCreateRequest;
import com.example.reservation.appointment.dto.AppointmentQueryRequest;
import com.example.reservation.appointment.dto.AppointmentResponse;
import com.example.reservation.appointment.service.AppointmentService;
import com.example.reservation.common.ApiResponse;
import com.example.reservation.meetingroom.dto.PageResponse;
import com.example.reservation.security.JwtUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ApiResponse<AppointmentResponse> create(
            @Valid @RequestBody AppointmentCreateRequest request,
            @AuthenticationPrincipal JwtUser currentUser
    ) {
        return ApiResponse.success(appointmentService.create(request, currentUser));
    }

    @GetMapping
    public ApiResponse<PageResponse<AppointmentResponse>> page(
            @ModelAttribute AppointmentQueryRequest request,
            @AuthenticationPrincipal JwtUser currentUser
    ) {
        return ApiResponse.success(appointmentService.page(request, currentUser));
    }

    @GetMapping("/{id}")
    public ApiResponse<AppointmentResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUser currentUser
    ) {
        return ApiResponse.success(appointmentService.getById(id, currentUser));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentCancelRequest request,
            @AuthenticationPrincipal JwtUser currentUser
    ) {
        appointmentService.cancel(id, request, currentUser);
        return ApiResponse.success();
    }
}


package com.example.reservation.notification.controller;

import com.example.reservation.common.ApiResponse;
import com.example.reservation.meetingroom.dto.PageResponse;
import com.example.reservation.notification.dto.NotificationQueryRequest;
import com.example.reservation.notification.dto.NotificationResponse;
import com.example.reservation.notification.service.NotificationService;
import com.example.reservation.security.JwtUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> page(
            @ModelAttribute NotificationQueryRequest request,
            @AuthenticationPrincipal JwtUser currentUser
    ) {
        return ApiResponse.success(notificationService.page(request, currentUser));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUser currentUser
    ) {
        notificationService.markRead(id, currentUser);
        return ApiResponse.success();
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal JwtUser currentUser) {
        notificationService.markAllRead(currentUser);
        return ApiResponse.success();
    }
}

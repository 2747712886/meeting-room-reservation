package com.example.reservation.notification.dto;

import com.example.reservation.domain.entity.Notification;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        Long appointmentId,
        String eventType,
        String title,
        String content,
        Boolean readFlag,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getAppointmentId(),
                notification.getEventType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getReadFlag(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}

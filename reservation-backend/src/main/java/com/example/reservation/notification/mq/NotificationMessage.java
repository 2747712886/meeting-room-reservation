package com.example.reservation.notification.mq;

public record NotificationMessage(
        Long userId,
        Long appointmentId,
        String eventType,
        String title,
        String content
) {
}

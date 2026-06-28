package com.example.reservation.appointment.dto;

import com.example.reservation.domain.entity.Appointment;
import com.example.reservation.domain.enums.AppointmentStatus;
import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long userId,
        Long roomId,
        String subject,
        LocalDateTime startTime,
        LocalDateTime endTime,
        AppointmentStatus status,
        String rejectReason,
        String cancelReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getUserId(),
                appointment.getRoomId(),
                appointment.getSubject(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getRejectReason(),
                appointment.getCancelReason(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}


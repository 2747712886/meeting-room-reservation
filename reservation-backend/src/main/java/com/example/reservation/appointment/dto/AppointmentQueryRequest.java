package com.example.reservation.appointment.dto;

import com.example.reservation.domain.enums.AppointmentStatus;
import java.time.LocalDateTime;

public record AppointmentQueryRequest(
        Long roomId,
        Long userId,
        AppointmentStatus status,
        LocalDateTime startFrom,
        LocalDateTime startTo,
        Integer page,
        Integer size
) {

    public int pageOrDefault() {
        return page == null || page < 1 ? 1 : page;
    }

    public int sizeOrDefault() {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }
}


package com.example.reservation.appointment.dto;

import jakarta.validation.constraints.Size;

public record AppointmentCancelRequest(
        @Size(max = 255) String cancelReason
) {
}


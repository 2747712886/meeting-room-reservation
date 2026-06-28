package com.example.reservation.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppointmentRejectRequest(
        @NotBlank
        @Size(max = 255)
        String rejectReason
) {
}

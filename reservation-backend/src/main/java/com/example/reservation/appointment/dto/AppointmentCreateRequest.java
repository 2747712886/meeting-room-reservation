package com.example.reservation.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record AppointmentCreateRequest(
        @NotNull Long roomId,
        @NotBlank @Size(max = 100) String subject,
        @NotNull @Future LocalDateTime startTime,
        @NotNull @Future LocalDateTime endTime
) {
}


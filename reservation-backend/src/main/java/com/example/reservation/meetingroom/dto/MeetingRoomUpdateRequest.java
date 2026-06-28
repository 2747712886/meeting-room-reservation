package com.example.reservation.meetingroom.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MeetingRoomUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 50) String floor,
        @NotNull @Min(1) Integer capacity,
        @NotNull Boolean hasProjector,
        @NotNull Boolean hasWhiteboard,
        @NotNull Boolean enabled
) {
}


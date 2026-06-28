package com.example.reservation.meetingroom.dto;

import com.example.reservation.domain.entity.MeetingRoom;
import java.time.LocalDateTime;

public record MeetingRoomResponse(
        Long id,
        String name,
        String floor,
        Integer capacity,
        Boolean hasProjector,
        Boolean hasWhiteboard,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static MeetingRoomResponse from(MeetingRoom room) {
        return new MeetingRoomResponse(
                room.getId(),
                room.getName(),
                room.getFloor(),
                room.getCapacity(),
                room.getHasProjector(),
                room.getHasWhiteboard(),
                room.getEnabled(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }
}


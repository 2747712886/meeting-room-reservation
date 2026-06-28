package com.example.reservation.meetingroom.dto;

public record MeetingRoomQueryRequest(
        String keyword,
        Boolean enabled,
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


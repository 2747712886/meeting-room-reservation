package com.example.reservation.meetingroom.controller;

import com.example.reservation.common.ApiResponse;
import com.example.reservation.meetingroom.dto.MeetingRoomCreateRequest;
import com.example.reservation.meetingroom.dto.MeetingRoomQueryRequest;
import com.example.reservation.meetingroom.dto.MeetingRoomResponse;
import com.example.reservation.meetingroom.dto.MeetingRoomUpdateRequest;
import com.example.reservation.meetingroom.dto.PageResponse;
import com.example.reservation.meetingroom.service.MeetingRoomService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meeting-rooms")
public class MeetingRoomController {

    private final MeetingRoomService meetingRoomService;

    public MeetingRoomController(MeetingRoomService meetingRoomService) {
        this.meetingRoomService = meetingRoomService;
    }

    @GetMapping
    public ApiResponse<PageResponse<MeetingRoomResponse>> page(@ModelAttribute MeetingRoomQueryRequest request) {
        return ApiResponse.success(meetingRoomService.page(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<MeetingRoomResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(meetingRoomService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MeetingRoomResponse> create(@Valid @RequestBody MeetingRoomCreateRequest request) {
        return ApiResponse.success(meetingRoomService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MeetingRoomResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MeetingRoomUpdateRequest request
    ) {
        return ApiResponse.success(meetingRoomService.update(id, request));
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> disable(@PathVariable Long id) {
        meetingRoomService.disable(id);
        return ApiResponse.success();
    }
}


package com.example.reservation.meetingroom.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reservation.common.ErrorCode;
import com.example.reservation.domain.entity.MeetingRoom;
import com.example.reservation.exception.BusinessException;
import com.example.reservation.mapper.MeetingRoomMapper;
import com.example.reservation.meetingroom.dto.MeetingRoomCreateRequest;
import com.example.reservation.meetingroom.dto.MeetingRoomQueryRequest;
import com.example.reservation.meetingroom.dto.MeetingRoomResponse;
import com.example.reservation.meetingroom.dto.MeetingRoomUpdateRequest;
import com.example.reservation.meetingroom.dto.PageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MeetingRoomService {

    private final MeetingRoomMapper meetingRoomMapper;

    public MeetingRoomService(MeetingRoomMapper meetingRoomMapper) {
        this.meetingRoomMapper = meetingRoomMapper;
    }

    public PageResponse<MeetingRoomResponse> page(MeetingRoomQueryRequest request) {
        Page<MeetingRoom> page = new Page<>(request.pageOrDefault(), request.sizeOrDefault());
        LambdaQueryWrapper<MeetingRoom> query = new LambdaQueryWrapper<MeetingRoom>()
                .eq(request.enabled() != null, MeetingRoom::getEnabled, request.enabled())
                .and(StringUtils.hasText(request.keyword()), wrapper -> wrapper
                        .like(MeetingRoom::getName, request.keyword())
                        .or()
                        .like(MeetingRoom::getFloor, request.keyword()))
                .orderByDesc(MeetingRoom::getCreatedAt);
        Page<MeetingRoom> result = meetingRoomMapper.selectPage(page, query);
        List<MeetingRoomResponse> records = result.getRecords()
                .stream()
                .map(MeetingRoomResponse::from)
                .toList();
        return PageResponse.from(result, records);
    }

    public MeetingRoomResponse getById(Long id) {
        return MeetingRoomResponse.from(findById(id));
    }

    public MeetingRoomResponse create(MeetingRoomCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        MeetingRoom room = new MeetingRoom();
        room.setName(request.name());
        room.setFloor(request.floor());
        room.setCapacity(request.capacity());
        room.setHasProjector(request.hasProjector());
        room.setHasWhiteboard(request.hasWhiteboard());
        room.setEnabled(true);
        room.setCreatedAt(now);
        room.setUpdatedAt(now);
        meetingRoomMapper.insert(room);
        return MeetingRoomResponse.from(room);
    }

    public MeetingRoomResponse update(Long id, MeetingRoomUpdateRequest request) {
        MeetingRoom room = findById(id);
        room.setName(request.name());
        room.setFloor(request.floor());
        room.setCapacity(request.capacity());
        room.setHasProjector(request.hasProjector());
        room.setHasWhiteboard(request.hasWhiteboard());
        room.setEnabled(request.enabled());
        room.setUpdatedAt(LocalDateTime.now());
        meetingRoomMapper.updateById(room);
        return MeetingRoomResponse.from(room);
    }

    public void disable(Long id) {
        MeetingRoom room = findById(id);
        room.setEnabled(false);
        room.setUpdatedAt(LocalDateTime.now());
        meetingRoomMapper.updateById(room);
    }

    private MeetingRoom findById(Long id) {
        MeetingRoom room = meetingRoomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "meeting room not found");
        }
        return room;
    }
}


package com.example.reservation.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reservation.appointment.dto.AppointmentCancelRequest;
import com.example.reservation.appointment.dto.AppointmentCreateRequest;
import com.example.reservation.appointment.dto.AppointmentQueryRequest;
import com.example.reservation.appointment.dto.AppointmentResponse;
import com.example.reservation.common.ErrorCode;
import com.example.reservation.domain.entity.Appointment;
import com.example.reservation.domain.entity.MeetingRoom;
import com.example.reservation.domain.enums.AppointmentStatus;
import com.example.reservation.exception.BusinessException;
import com.example.reservation.mapper.AppointmentMapper;
import com.example.reservation.mapper.MeetingRoomMapper;
import com.example.reservation.meetingroom.dto.PageResponse;
import com.example.reservation.security.JwtUser;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final MeetingRoomMapper meetingRoomMapper;

    public AppointmentService(AppointmentMapper appointmentMapper, MeetingRoomMapper meetingRoomMapper) {
        this.appointmentMapper = appointmentMapper;
        this.meetingRoomMapper = meetingRoomMapper;
    }

    @Transactional
    public AppointmentResponse create(AppointmentCreateRequest request, JwtUser currentUser) {
        validateTimeRange(request.startTime(), request.endTime());
        MeetingRoom room = meetingRoomMapper.selectById(request.roomId());
        if (room == null || !Boolean.TRUE.equals(room.getEnabled())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "meeting room not found or disabled");
        }

        LocalDateTime now = LocalDateTime.now();
        Appointment appointment = new Appointment();
        appointment.setUserId(currentUser.userId());
        appointment.setRoomId(request.roomId());
        appointment.setSubject(request.subject());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);
        appointmentMapper.insert(appointment);
        return AppointmentResponse.from(appointment);
    }

    public PageResponse<AppointmentResponse> page(AppointmentQueryRequest request, JwtUser currentUser) {
        boolean admin = hasRole(currentUser, "ROLE_ADMIN");
        Long queryUserId = admin ? request.userId() : currentUser.userId();
        Page<Appointment> page = new Page<>(request.pageOrDefault(), request.sizeOrDefault());
        LambdaQueryWrapper<Appointment> query = new LambdaQueryWrapper<Appointment>()
                .eq(queryUserId != null, Appointment::getUserId, queryUserId)
                .eq(request.roomId() != null, Appointment::getRoomId, request.roomId())
                .eq(request.status() != null, Appointment::getStatus, request.status())
                .ge(request.startFrom() != null, Appointment::getStartTime, request.startFrom())
                .le(request.startTo() != null, Appointment::getStartTime, request.startTo())
                .orderByDesc(Appointment::getCreatedAt);
        Page<Appointment> result = appointmentMapper.selectPage(page, query);
        List<AppointmentResponse> records = result.getRecords()
                .stream()
                .map(AppointmentResponse::from)
                .toList();
        return PageResponse.from(result, records);
    }

    public AppointmentResponse getById(Long id, JwtUser currentUser) {
        Appointment appointment = findById(id);
        ensureOwnerOrAdmin(appointment, currentUser);
        return AppointmentResponse.from(appointment);
    }

    @Transactional
    public void cancel(Long id, AppointmentCancelRequest request, JwtUser currentUser) {
        Appointment appointment = findById(id);
        ensureOwnerOrAdmin(appointment, currentUser);
        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.APPROVED) {
            throw new BusinessException(ErrorCode.CONFLICT, "only pending or approved appointments can be cancelled");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelReason(request.cancelReason());
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
    }

    private Appointment findById(Long id) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "appointment not found");
        }
        return appointment;
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "start time must be before end time");
        }
    }

    private void ensureOwnerOrAdmin(Appointment appointment, JwtUser currentUser) {
        if (!appointment.getUserId().equals(currentUser.userId()) && !hasRole(currentUser, "ROLE_ADMIN")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "appointment is not accessible");
        }
    }

    private boolean hasRole(JwtUser currentUser, String role) {
        return currentUser.authorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}


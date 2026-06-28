package com.example.reservation.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reservation.appointment.dto.AppointmentCancelRequest;
import com.example.reservation.appointment.dto.AppointmentCreateRequest;
import com.example.reservation.appointment.dto.AppointmentQueryRequest;
import com.example.reservation.appointment.dto.AppointmentRejectRequest;
import com.example.reservation.appointment.dto.AppointmentResponse;
import com.example.reservation.common.ErrorCode;
import com.example.reservation.domain.entity.Appointment;
import com.example.reservation.domain.entity.AppointmentLog;
import com.example.reservation.domain.entity.MeetingRoom;
import com.example.reservation.domain.enums.AppointmentStatus;
import com.example.reservation.exception.BusinessException;
import com.example.reservation.mapper.AppointmentLogMapper;
import com.example.reservation.mapper.AppointmentMapper;
import com.example.reservation.mapper.MeetingRoomMapper;
import com.example.reservation.meetingroom.dto.PageResponse;
import com.example.reservation.security.JwtUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    private static final long APPOINTMENT_LOCK_WAIT_SECONDS = 3;
    private static final long APPOINTMENT_LOCK_LEASE_SECONDS = 10;

    private final AppointmentMapper appointmentMapper;
    private final AppointmentLogMapper appointmentLogMapper;
    private final MeetingRoomMapper meetingRoomMapper;
    private final RedissonClient redissonClient;

    public AppointmentService(
            AppointmentMapper appointmentMapper,
            AppointmentLogMapper appointmentLogMapper,
            MeetingRoomMapper meetingRoomMapper,
            RedissonClient redissonClient) {
        this.appointmentMapper = appointmentMapper;
        this.appointmentLogMapper = appointmentLogMapper;
        this.meetingRoomMapper = meetingRoomMapper;
        this.redissonClient = redissonClient;
    }

    @Transactional
    public AppointmentResponse create(AppointmentCreateRequest request, JwtUser currentUser) {
        validateTimeRange(request.startTime(), request.endTime());
        MeetingRoom room = meetingRoomMapper.selectById(request.roomId());
        if (room == null || !Boolean.TRUE.equals(room.getEnabled())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "meeting room not found or disabled");
        }

        String lockKey = buildAppointmentLockKey(request.roomId(), request.startTime());
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(
                    APPOINTMENT_LOCK_WAIT_SECONDS,
                    APPOINTMENT_LOCK_LEASE_SECONDS,
                    TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(ErrorCode.CONFLICT, "appointment creation is busy, please retry");
            }
            return createWithLock(request, currentUser);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "appointment creation was interrupted");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private AppointmentResponse createWithLock(AppointmentCreateRequest request, JwtUser currentUser) {
        ensureNoTimeConflict(request.roomId(), request.startTime(), request.endTime());
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
        createStatusLog(appointment, null, AppointmentStatus.PENDING, currentUser.userId(), "create appointment");
        return AppointmentResponse.from(appointment);
    }

    private String buildAppointmentLockKey(Long roomId, LocalDateTime startTime) {
        return "lock:appointment:room:%d:%s".formatted(roomId, startTime.toLocalDate());
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
    public AppointmentResponse approve(Long id, JwtUser currentUser) {
        Appointment appointment = findById(id);
        ensurePending(appointment);
        ensureNoTimeConflict(
                appointment.getRoomId(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getId());
        appointment.setStatus(AppointmentStatus.APPROVED);
        appointment.setRejectReason(null);
        appointment.setCancelReason(null);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
        createStatusLog(
                appointment,
                AppointmentStatus.PENDING,
                AppointmentStatus.APPROVED,
                currentUser.userId(),
                "approve appointment");
        return AppointmentResponse.from(appointment);
    }

    @Transactional
    public AppointmentResponse reject(Long id, AppointmentRejectRequest request, JwtUser currentUser) {
        Appointment appointment = findById(id);
        ensurePending(appointment);
        appointment.setStatus(AppointmentStatus.REJECTED);
        appointment.setRejectReason(request.rejectReason());
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
        createStatusLog(
                appointment,
                AppointmentStatus.PENDING,
                AppointmentStatus.REJECTED,
                currentUser.userId(),
                request.rejectReason());
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
        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelReason(request.cancelReason());
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
        createStatusLog(appointment, oldStatus, AppointmentStatus.CANCELLED, currentUser.userId(), request.cancelReason());
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

    private void ensureNoTimeConflict(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        ensureNoTimeConflict(roomId, startTime, endTime, null);
    }

    private void ensureNoTimeConflict(Long roomId, LocalDateTime startTime, LocalDateTime endTime, Long excludedId) {
        Long conflictCount = appointmentMapper.selectCount(new LambdaQueryWrapper<Appointment>()
                .ne(excludedId != null, Appointment::getId, excludedId)
                .eq(Appointment::getRoomId, roomId)
                .in(Appointment::getStatus, AppointmentStatus.PENDING, AppointmentStatus.APPROVED)
                .lt(Appointment::getStartTime, endTime)
                .gt(Appointment::getEndTime, startTime));
        if (conflictCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "appointment time conflicts with existing reservation");
        }
    }

    private void ensurePending(Appointment appointment) {
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "only pending appointments can be processed");
        }
    }

    private void ensureOwnerOrAdmin(Appointment appointment, JwtUser currentUser) {
        if (!appointment.getUserId().equals(currentUser.userId()) && !hasRole(currentUser, "ROLE_ADMIN")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "appointment is not accessible");
        }
    }

    private void createStatusLog(
            Appointment appointment,
            AppointmentStatus oldStatus,
            AppointmentStatus newStatus,
            Long operatorId,
            String remark) {
        LocalDateTime now = LocalDateTime.now();
        AppointmentLog log = new AppointmentLog();
        log.setAppointmentId(appointment.getId());
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        appointmentLogMapper.insert(log);
    }

    private boolean hasRole(JwtUser currentUser, String role) {
        return currentUser.authorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}

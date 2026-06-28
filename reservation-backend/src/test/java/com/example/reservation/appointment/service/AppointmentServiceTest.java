package com.example.reservation.appointment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.reservation.appointment.dto.AppointmentCreateRequest;
import com.example.reservation.appointment.dto.AppointmentRejectRequest;
import com.example.reservation.common.ErrorCode;
import com.example.reservation.domain.entity.Appointment;
import com.example.reservation.domain.entity.MeetingRoom;
import com.example.reservation.domain.enums.AppointmentStatus;
import com.example.reservation.exception.BusinessException;
import com.example.reservation.mapper.AppointmentMapper;
import com.example.reservation.mapper.MeetingRoomMapper;
import com.example.reservation.security.JwtUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private MeetingRoomMapper meetingRoomMapper;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @Test
    void createUsesRoomDateLockAroundConflictCheckAndInsert() throws InterruptedException {
        AppointmentService service = new AppointmentService(appointmentMapper, meetingRoomMapper, redissonClient);
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                10L,
                "weekly sync",
                LocalDateTime.of(2026, 6, 28, 9, 0),
                LocalDateTime.of(2026, 6, 28, 10, 0));
        MeetingRoom room = new MeetingRoom();
        room.setId(10L);
        room.setEnabled(true);
        when(meetingRoomMapper.selectById(10L)).thenReturn(room);
        when(redissonClient.getLock("lock:appointment:room:10:2026-06-28")).thenReturn(lock);
        when(lock.tryLock(3, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        when(appointmentMapper.selectCount(any())).thenReturn(0L);

        service.create(request, currentUser());

        verify(redissonClient).getLock("lock:appointment:room:10:2026-06-28");
        verify(appointmentMapper).selectCount(any());
        verify(appointmentMapper).insert(any(Appointment.class));
        verify(lock).unlock();
    }

    @Test
    void createFailsWhenAppointmentLockCannotBeAcquired() throws InterruptedException {
        AppointmentService service = new AppointmentService(appointmentMapper, meetingRoomMapper, redissonClient);
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                10L,
                "weekly sync",
                LocalDateTime.of(2026, 6, 28, 9, 0),
                LocalDateTime.of(2026, 6, 28, 10, 0));
        MeetingRoom room = new MeetingRoom();
        room.setId(10L);
        room.setEnabled(true);
        when(meetingRoomMapper.selectById(10L)).thenReturn(room);
        when(redissonClient.getLock("lock:appointment:room:10:2026-06-28")).thenReturn(lock);
        when(lock.tryLock(3, 10, TimeUnit.SECONDS)).thenReturn(false);

        assertThatThrownBy(() -> service.create(request, currentUser()))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.CONFLICT));

        verify(appointmentMapper, never()).selectCount(any());
        verify(appointmentMapper, never()).insert(any(Appointment.class));
        verify(lock, never()).unlock();
    }

    @Test
    void createRestoresInterruptFlagWhenLockWaitIsInterrupted() throws InterruptedException {
        AppointmentService service = new AppointmentService(appointmentMapper, meetingRoomMapper, redissonClient);
        AppointmentCreateRequest request = new AppointmentCreateRequest(
                10L,
                "weekly sync",
                LocalDateTime.of(2026, 6, 28, 9, 0),
                LocalDateTime.of(2026, 6, 28, 10, 0));
        MeetingRoom room = new MeetingRoom();
        room.setId(10L);
        room.setEnabled(true);
        when(meetingRoomMapper.selectById(10L)).thenReturn(room);
        when(redissonClient.getLock("lock:appointment:room:10:2026-06-28")).thenReturn(lock);
        when(lock.tryLock(3, 10, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        assertThatThrownBy(() -> service.create(request, currentUser()))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.INTERNAL_ERROR));
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        Thread.interrupted();

        verify(appointmentMapper, never()).selectCount(any());
        verify(appointmentMapper, never()).insert(any(Appointment.class));
    }

    @Test
    void approvePendingAppointmentWhenNoConflictExists() {
        AppointmentService service = new AppointmentService(appointmentMapper, meetingRoomMapper, redissonClient);
        Appointment appointment = pendingAppointment();
        when(appointmentMapper.selectById(100L)).thenReturn(appointment);
        when(appointmentMapper.selectCount(any())).thenReturn(0L);

        service.approve(100L);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.APPROVED);
        assertThat(appointment.getRejectReason()).isNull();
        assertThat(appointment.getCancelReason()).isNull();
        verify(appointmentMapper).updateById(appointment);
    }

    @Test
    void approveFailsWhenAppointmentHasConflict() {
        AppointmentService service = new AppointmentService(appointmentMapper, meetingRoomMapper, redissonClient);
        Appointment appointment = pendingAppointment();
        when(appointmentMapper.selectById(100L)).thenReturn(appointment);
        when(appointmentMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.approve(100L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.CONFLICT));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        verify(appointmentMapper, never()).updateById(any(Appointment.class));
    }

    @Test
    void rejectPendingAppointmentStoresReason() {
        AppointmentService service = new AppointmentService(appointmentMapper, meetingRoomMapper, redissonClient);
        Appointment appointment = pendingAppointment();
        when(appointmentMapper.selectById(100L)).thenReturn(appointment);

        service.reject(100L, new AppointmentRejectRequest("time unavailable"));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.REJECTED);
        assertThat(appointment.getRejectReason()).isEqualTo("time unavailable");
        verify(appointmentMapper).updateById(appointment);
    }

    @Test
    void rejectFailsWhenAppointmentIsNotPending() {
        AppointmentService service = new AppointmentService(appointmentMapper, meetingRoomMapper, redissonClient);
        Appointment appointment = pendingAppointment();
        appointment.setStatus(AppointmentStatus.APPROVED);
        when(appointmentMapper.selectById(100L)).thenReturn(appointment);

        assertThatThrownBy(() -> service.reject(100L, new AppointmentRejectRequest("time unavailable")))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.CONFLICT));

        verify(appointmentMapper, never()).updateById(any(Appointment.class));
    }

    private Appointment pendingAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(100L);
        appointment.setUserId(1L);
        appointment.setRoomId(10L);
        appointment.setSubject("weekly sync");
        appointment.setStartTime(LocalDateTime.of(2026, 6, 28, 9, 0));
        appointment.setEndTime(LocalDateTime.of(2026, 6, 28, 10, 0));
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setRejectReason("old reason");
        appointment.setCancelReason("old cancel reason");
        return appointment;
    }

    private JwtUser currentUser() {
        return new JwtUser(1L, "user", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}

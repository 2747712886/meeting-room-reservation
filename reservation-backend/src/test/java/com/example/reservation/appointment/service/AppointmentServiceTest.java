package com.example.reservation.appointment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.reservation.appointment.dto.AppointmentCreateRequest;
import com.example.reservation.common.ErrorCode;
import com.example.reservation.domain.entity.Appointment;
import com.example.reservation.domain.entity.MeetingRoom;
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

    private JwtUser currentUser() {
        return new JwtUser(1L, "user", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}

package com.example.reservation.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.example.reservation.common.ErrorCode;
import com.example.reservation.domain.entity.Notification;
import com.example.reservation.exception.BusinessException;
import com.example.reservation.mapper.NotificationMapper;
import com.example.reservation.security.JwtUser;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationMapper notificationMapper;

    @Test
    void markReadUpdatesUnreadOwnedNotification() {
        NotificationService service = new NotificationService(notificationMapper);
        Notification notification = notification(false, 1L);
        when(notificationMapper.selectById(100L)).thenReturn(notification);

        service.markRead(100L, currentUser());

        assertThat(notification.getReadFlag()).isTrue();
        assertThat(notification.getUpdatedAt()).isNotNull();
        verify(notificationMapper).updateById(notification);
    }

    @Test
    void markReadIgnoresAlreadyReadNotification() {
        NotificationService service = new NotificationService(notificationMapper);
        Notification notification = notification(true, 1L);
        when(notificationMapper.selectById(100L)).thenReturn(notification);

        service.markRead(100L, currentUser());

        verify(notificationMapper, never()).updateById(any(Notification.class));
    }

    @Test
    void markReadRejectsOtherUsersNotification() {
        NotificationService service = new NotificationService(notificationMapper);
        Notification notification = notification(false, 2L);
        when(notificationMapper.selectById(100L)).thenReturn(notification);

        assertThatThrownBy(() -> service.markRead(100L, currentUser()))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_FOUND));

        verify(notificationMapper, never()).updateById(any(Notification.class));
    }

    @Test
    void markAllReadUpdatesOnlyCurrentUsersUnreadNotifications() {
        NotificationService service = new NotificationService(notificationMapper);

        service.markAllRead(currentUser());

        verify(notificationMapper).update(any(Notification.class), any(Wrapper.class));
    }

    private Notification notification(boolean readFlag, Long userId) {
        Notification notification = new Notification();
        notification.setId(100L);
        notification.setUserId(userId);
        notification.setReadFlag(readFlag);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        return notification;
    }

    private JwtUser currentUser() {
        return new JwtUser(1L, "user", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}

package com.example.reservation.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reservation.common.ErrorCode;
import com.example.reservation.domain.entity.Notification;
import com.example.reservation.exception.BusinessException;
import com.example.reservation.mapper.NotificationMapper;
import com.example.reservation.meetingroom.dto.PageResponse;
import com.example.reservation.notification.dto.NotificationQueryRequest;
import com.example.reservation.notification.dto.NotificationResponse;
import com.example.reservation.notification.mq.NotificationMessage;
import com.example.reservation.security.JwtUser;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    public PageResponse<NotificationResponse> page(NotificationQueryRequest request, JwtUser currentUser) {
        Page<Notification> page = new Page<>(request.pageOrDefault(), request.sizeOrDefault());
        LambdaQueryWrapper<Notification> query = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, currentUser.userId())
                .eq(request.readFlag() != null, Notification::getReadFlag, request.readFlag())
                .orderByDesc(Notification::getCreatedAt);
        Page<Notification> result = notificationMapper.selectPage(page, query);
        List<NotificationResponse> records = result.getRecords()
                .stream()
                .map(NotificationResponse::from)
                .toList();
        return PageResponse.from(result, records);
    }

    @Transactional
    public void create(NotificationMessage message) {
        LocalDateTime now = LocalDateTime.now();
        Notification notification = new Notification();
        notification.setUserId(message.userId());
        notification.setAppointmentId(message.appointmentId());
        notification.setEventType(message.eventType());
        notification.setTitle(message.title());
        notification.setContent(message.content());
        notification.setReadFlag(false);
        notification.setCreatedAt(now);
        notification.setUpdatedAt(now);
        notificationMapper.insert(notification);
    }

    @Transactional
    public void markRead(Long id, JwtUser currentUser) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null || !notification.getUserId().equals(currentUser.userId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "notification not found");
        }
        if (Boolean.TRUE.equals(notification.getReadFlag())) {
            return;
        }
        notification.setReadFlag(true);
        notification.setUpdatedAt(LocalDateTime.now());
        notificationMapper.updateById(notification);
    }

    @Transactional
    public void markAllRead(JwtUser currentUser) {
        Notification update = new Notification();
        update.setReadFlag(true);
        update.setUpdatedAt(LocalDateTime.now());
        notificationMapper.update(update, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, currentUser.userId())
                .eq(Notification::getReadFlag, false));
    }
}

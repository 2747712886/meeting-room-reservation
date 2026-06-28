package com.example.reservation.notification.mq;

import com.example.reservation.config.NotificationRabbitConfig;
import com.example.reservation.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private final NotificationService notificationService;

    public NotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = NotificationRabbitConfig.NOTIFICATION_QUEUE)
    public void handle(NotificationMessage message) {
        notificationService.create(message);
    }
}

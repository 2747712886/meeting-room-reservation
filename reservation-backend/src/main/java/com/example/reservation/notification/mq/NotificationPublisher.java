package com.example.reservation.notification.mq;

import com.example.reservation.config.NotificationRabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(NotificationMessage message) {
        rabbitTemplate.convertAndSend(
                NotificationRabbitConfig.NOTIFICATION_EXCHANGE,
                NotificationRabbitConfig.NOTIFICATION_ROUTING_KEY,
                message);
    }
}

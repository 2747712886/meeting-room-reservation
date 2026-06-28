package com.example.reservation.notification.mq;

import static org.mockito.Mockito.verify;

import com.example.reservation.config.NotificationRabbitConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class NotificationPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publishSendsMessageToNotificationExchange() {
        NotificationPublisher publisher = new NotificationPublisher(rabbitTemplate);
        NotificationMessage message = new NotificationMessage(
                1L,
                100L,
                "APPOINTMENT_APPROVED",
                "Appointment approved",
                "Your appointment has been approved");

        publisher.publish(message);

        verify(rabbitTemplate).convertAndSend(
                NotificationRabbitConfig.NOTIFICATION_EXCHANGE,
                NotificationRabbitConfig.NOTIFICATION_ROUTING_KEY,
                message);
    }
}

package com.example.reservation.notification.mq;

import static org.mockito.Mockito.verify;

import com.example.reservation.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @Test
    void handleDelegatesMessageToNotificationService() {
        NotificationListener listener = new NotificationListener(notificationService);
        NotificationMessage message = new NotificationMessage(
                1L,
                100L,
                "APPOINTMENT_APPROVED",
                "Appointment approved",
                "Your appointment has been approved");

        listener.handle(message);

        verify(notificationService).create(message);
    }
}

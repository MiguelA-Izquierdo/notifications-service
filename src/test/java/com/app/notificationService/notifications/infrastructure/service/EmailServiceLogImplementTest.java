package com.app.notificationService.notifications.infrastructure.service;

import com.app.notificationService.notifications.domain.model.User;
import com.app.notificationService.notifications.domain.model.UserCreatedEmailNotification;
import com.app.notificationService.notifications.domain.valueObject.notification.Email;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;

class EmailServiceLogImplementTest {

    private final EmailServiceLogImplement service = new EmailServiceLogImplement();

    @Test
    void shouldNotThrowWhenSendingEmail() {
        User user = User.of(UUID.randomUUID(), "Miguel", "Izquierdo", "miguel@example.com");
        UserCreatedEmailNotification notification = UserCreatedEmailNotification.of(
                List.of(Email.of("miguel@example.com")), user
        );

        assertThatCode(() -> service.sendEmail(notification)).doesNotThrowAnyException();
    }
}
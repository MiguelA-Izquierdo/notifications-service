package com.app.notificationService.notifications.infrastructure.service;

import com.app.notificationService.notifications.helpers.NotificationTestFactory;
import com.app.notificationService.notifications.helpers.UserTestFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class EmailServiceLogImplementTest {

    private final EmailServiceLogImplement service = new EmailServiceLogImplement();

    @Test
    void shouldNotThrowWhenSendingEmail() {
        assertThatCode(() -> service.sendEmail(
            NotificationTestFactory.createdNotification(UserTestFactory.defaultUser())
        )).doesNotThrowAnyException();
    }
}
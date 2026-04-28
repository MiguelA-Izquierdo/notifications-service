package com.app.notificationService.notifications.application.events.handlers;

import com.app.notificationService.notifications.application.events.UserCreatedEvent;
import com.app.notificationService.notifications.domain.model.EmailNotification;
import com.app.notificationService.notifications.domain.model.UserCreatedEmailNotification;
import com.app.notificationService.notifications.domain.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCreatedEventHandlerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserCreatedEventHandler handler;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private UserCreatedEvent buildEvent() {
        return new UserCreatedEvent(
            new UserCreatedEvent.UserPayload(USER_ID, "Miguel", "Izquierdo", "miguel@example.com")
        );
    }

    @Test
    void shouldDelegateToEmailService() {
        handler.handle(buildEvent());

        verify(emailService).sendEmail(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldSendCorrectNotificationType() {
        ArgumentCaptor<EmailNotification<?>> captor = ArgumentCaptor.forClass(EmailNotification.class);

        handler.handle(buildEvent());

        verify(emailService).sendEmail(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(UserCreatedEmailNotification.class);
    }

    @Test
    void shouldSendToUserEmail() {
        ArgumentCaptor<EmailNotification<?>> captor = ArgumentCaptor.forClass(EmailNotification.class);

        handler.handle(buildEvent());

        verify(emailService).sendEmail(captor.capture());
        EmailNotification<?> notification = captor.getValue();
        assertThat(notification.getRecipientsEmail()).hasSize(1);
        assertThat(notification.getRecipientsEmail().get(0).getEmail()).isEqualTo("miguel@example.com");
    }

    @Test
    void shouldBuildSubjectWithUserName() {
        ArgumentCaptor<EmailNotification<?>> captor = ArgumentCaptor.forClass(EmailNotification.class);

        handler.handle(buildEvent());

        verify(emailService).sendEmail(captor.capture());
        assertThat(captor.getValue().getSubject().getValue()).contains("Miguel");
    }
}
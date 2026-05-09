package com.app.notificationService.notifications.application.events.handlers;

import com.app.notificationService.notifications.application.events.UserCreatedEvent;
import com.app.notificationService.notifications.domain.model.EmailNotification;
import com.app.notificationService.notifications.domain.model.UserCreatedEmailNotification;
import com.app.notificationService.notifications.domain.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.UUID;

import com.app.notificationService.notifications.domain.exceptions.ValueObjectValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCreatedEventHandlerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private MessageSource messageSource;

    private UserCreatedEventHandler handler;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        handler = new UserCreatedEventHandler(emailService, messageSource, "es");
        lenient().when(messageSource.getMessage(eq("email.user-created.subject"), any(Object[].class), any(Locale.class)))
            .thenReturn("mocked-subject");
    }

    private UserCreatedEvent buildEvent() {
        return new UserCreatedEvent(
            UUID.fromString("00000000-0000-0000-0000-000000000099"),
            new UserCreatedEvent.UserPayload(USER_ID, "Miguel", "Izquierdo", "miguel@example.com")
        );
    }

    @Test
    void shouldDelegateToEmailService() {
        handler.handle(buildEvent());

        verify(emailService).sendEmail(any());
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
        assertThat(captor.getValue().getRecipientsEmail()).hasSize(1);
        assertThat(captor.getValue().getRecipientsEmail().get(0).getEmail()).isEqualTo("miguel@example.com");
    }

    @Test
    void shouldThrowWhenPayloadEmailIsInvalid() {
        UserCreatedEvent event = new UserCreatedEvent(
            UUID.fromString("00000000-0000-0000-0000-000000000099"),
            new UserCreatedEvent.UserPayload(USER_ID, "Miguel", "Izquierdo", "not-an-email")
        );

        assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(ValueObjectValidationException.class);
    }

    @Test
    void shouldBuildSubjectFromMessageSource() {
        ArgumentCaptor<EmailNotification<?>> captor = ArgumentCaptor.forClass(EmailNotification.class);

        handler.handle(buildEvent());

        verify(emailService).sendEmail(captor.capture());
        verify(messageSource).getMessage(eq("email.user-created.subject"), any(Object[].class), any(Locale.class));
        assertThat(captor.getValue().getSubject().getValue()).isEqualTo("mocked-subject");
    }
}
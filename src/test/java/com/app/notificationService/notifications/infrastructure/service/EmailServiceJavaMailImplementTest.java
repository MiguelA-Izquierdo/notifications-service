package com.app.notificationService.notifications.infrastructure.service;

import com.app.notificationService.notifications.domain.model.User;
import com.app.notificationService.notifications.domain.model.UserCreatedEmailNotification;
import com.app.notificationService.notifications.domain.valueObject.notification.Email;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceJavaMailImplementTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private TemplateEngine templateEngine;

    private EmailServiceJavaMailImplement service;

    @BeforeEach
    void setUp() {
        service = new EmailServiceJavaMailImplement(javaMailSender, templateEngine, "sender@example.com");
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((jakarta.mail.Session) null));
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn("<html>ok</html>");
    }

    private UserCreatedEmailNotification buildNotification() {
        User user = User.of(UUID.randomUUID(), "Miguel", "Izquierdo", "miguel@example.com");
        return UserCreatedEmailNotification.of(List.of(Email.of("miguel@example.com")), user);
    }

    @Test
    void shouldSendMimeMessage() {
        service.sendEmail(buildNotification());

        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void shouldProcessCorrectTemplate() {
        UserCreatedEmailNotification notification = buildNotification();

        service.sendEmail(notification);

        verify(templateEngine).process(eq("emails/user-created"), any(IContext.class));
    }

    @Test
    void shouldSetCorrectSubjectOnMimeMessage() throws Exception {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);

        service.sendEmail(buildNotification());

        verify(javaMailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).contains("Miguel");
    }

    @Test
    void shouldSetRecipientOnMimeMessage() throws Exception {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);

        service.sendEmail(buildNotification());

        verify(javaMailSender).send(captor.capture());
        String recipients = captor.getValue().getAllRecipients()[0].toString();
        assertThat(recipients).isEqualTo("miguel@example.com");
    }
}
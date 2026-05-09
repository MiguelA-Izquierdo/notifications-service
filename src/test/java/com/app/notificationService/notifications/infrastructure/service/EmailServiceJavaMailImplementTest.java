package com.app.notificationService.notifications.infrastructure.service;

import com.app.notificationService.notifications.helpers.NotificationTestFactory;
import com.app.notificationService.notifications.helpers.UserTestFactory;
import com.app.notificationService.notifications.domain.model.User;
import com.app.notificationService.notifications.domain.model.UserCreatedEmailNotification;
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

import com.app.notificationService.notifications.domain.exceptions.EmailSendingException;
import jakarta.mail.MessagingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
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
        service = new EmailServiceJavaMailImplement(javaMailSender, templateEngine, "sender@example.com", "es");
        lenient().when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((jakarta.mail.Session) null));
        lenient().when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn("<html>ok</html>");
    }

    private UserCreatedEmailNotification buildNotification() {
        User user = UserTestFactory.defaultUser();
        return NotificationTestFactory.createdNotification(user);
    }

    @Test
    void shouldSendMimeMessage() {
        service.sendEmail(buildNotification());

        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void shouldProcessCorrectTemplate() {
        service.sendEmail(buildNotification());

        verify(templateEngine).process(eq("emails/user-created"), any(IContext.class));
    }

    @Test
    void shouldSetCorrectSubjectOnMimeMessage() throws Exception {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);

        service.sendEmail(buildNotification());

        verify(javaMailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).isEqualTo("Test subject");
    }

    @Test
    void shouldSetRecipientOnMimeMessage() throws Exception {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);

        service.sendEmail(buildNotification());

        verify(javaMailSender).send(captor.capture());
        String recipient = captor.getValue().getAllRecipients()[0].toString();
        assertThat(recipient).isEqualTo("miguel@example.com");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldWrapMessagingExceptionInEmailSendingException() throws Exception {
        MimeMessage spyMessage = spy(new MimeMessage((jakarta.mail.Session) null));
        doThrow(new MessagingException("connection refused")).when(spyMessage).setSubject(any());
        when(javaMailSender.createMimeMessage()).thenReturn(spyMessage);

        assertThatThrownBy(() -> service.sendEmail(buildNotification()))
                .isInstanceOf(EmailSendingException.class)
                .hasMessageContaining("emails/user-created");
    }
}
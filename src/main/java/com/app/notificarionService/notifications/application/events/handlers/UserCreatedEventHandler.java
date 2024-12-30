package com.app.notificarionService.notifications.application.events.handlers;

import com.app.notificarionService.notifications.application.events.UserCreatedEvent;
import com.app.notificarionService.notifications.domain.model.UserCreatedEmailNotification;
import com.app.notificarionService.notifications.domain.model.User;
import com.app.notificarionService.notifications.domain.service.EmailService;
import com.app.notificarionService.notifications.domain.valueObject.notification.Email;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class UserCreatedEventHandler {

  private final EmailService emailService;

  public UserCreatedEventHandler(EmailService emailService) {
    this.emailService = emailService;
  }

  public CompletableFuture<Void> handle(UserCreatedEvent event) {
    return CompletableFuture.runAsync(() -> {
      try {
        UserCreatedEvent.UserPayload userPayload = event.getPayload();

        Email recipientEmail = Email.of(userPayload.email());
        User user = User.of(
          userPayload.userId(),
          userPayload.name(),
          userPayload.lastName(),
          userPayload.email()
        );

        UserCreatedEmailNotification notification = UserCreatedEmailNotification.of(
          List.of(recipientEmail),
          user
        );

        emailService.sendEmail(notification);
      } catch (MessagingException e) {
        throw new RuntimeException("Error al procesar el evento y enviar el correo", e);
      }
    });
  }

}


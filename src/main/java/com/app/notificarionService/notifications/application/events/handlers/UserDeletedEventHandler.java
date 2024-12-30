package com.app.notificarionService.notifications.application.events.handlers;

import com.app.notificarionService.notifications.application.events.UserDeletedEvent;
import com.app.notificarionService.notifications.domain.model.User;
import com.app.notificarionService.notifications.domain.model.UserDeletedEmailNotification;
import com.app.notificarionService.notifications.domain.service.EmailService;
import com.app.notificarionService.notifications.domain.valueObject.notification.Email;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDeletedEventHandler {

  private final EmailService emailService;

  public UserDeletedEventHandler(EmailService emailService) {
    this.emailService = emailService;
  }

  public void handle(UserDeletedEvent event) {
    UserDeletedEvent.UserPayload userPayload = event.getPayload();

    Email recipientEmail = Email.of(userPayload.email());
    User user = User.of(
      userPayload.userId(),
      userPayload.name(),
      userPayload.lastName(),
      userPayload.email()
    );

    UserDeletedEmailNotification userDeletedEmailNotification = UserDeletedEmailNotification.of(
      List.of(recipientEmail),
      user
    );

    try {
      emailService.sendEmail(userDeletedEmailNotification);
    } catch (MessagingException e) {
      throw new RuntimeException("Error al procesar el evento y enviar el correo", e);
    }

  }
}


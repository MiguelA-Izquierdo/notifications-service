package com.app.notificarionService.notifications.application.events.handlers;

import com.app.notificarionService.notifications.application.bus.command.NotificationsCommandBus;
import com.app.notificarionService.notifications.application.bus.command.SendEmailCommand;
import com.app.notificarionService.notifications.application.events.UserDeletedEvent;
import com.app.notificarionService.notifications.domain.model.UserCreatedEmailNotification;
import com.app.notificarionService.notifications.domain.model.User;
import com.app.notificarionService.notifications.domain.model.UserDeletedEmailNotification;
import com.app.notificarionService.notifications.domain.valueObject.notification.Email;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDeletedEventHandler {

  private final NotificationsCommandBus commandBus;

  public UserDeletedEventHandler(NotificationsCommandBus commandBus) {
    this.commandBus = commandBus;
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

    SendEmailCommand command = SendEmailCommand.of(userDeletedEmailNotification);
    commandBus.dispatch(command);
  }
}


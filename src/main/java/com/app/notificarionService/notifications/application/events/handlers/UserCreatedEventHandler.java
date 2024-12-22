package com.app.notificarionService.notifications.application.events.handlers;

import com.app.notificarionService.notifications.application.bus.command.NotificationsCommandBus;
import com.app.notificarionService.notifications.application.bus.command.SendEmailCommand;
import com.app.notificarionService.notifications.application.events.UserCreatedEvent;
import com.app.notificarionService.notifications.domain.model.UserCreatedEmailNotification;
import com.app.notificarionService.notifications.domain.model.User;
import com.app.notificarionService.notifications.domain.valueObject.notification.Email;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCreatedEventHandler {

  private final NotificationsCommandBus commandBus;

  public UserCreatedEventHandler(NotificationsCommandBus commandBus) {
    this.commandBus = commandBus;
  }

  public void handle(UserCreatedEvent event) {
    UserCreatedEvent.UserPayload userPayload = event.getPayload();

    Email recipientEmail = Email.of(userPayload.email());
    User user = User.of(
      userPayload.userId(),
      userPayload.name(),
      userPayload.lastName(),
      userPayload.email()
    );

    UserCreatedEmailNotification userCreatedEmailNotification = UserCreatedEmailNotification.of(
      List.of(recipientEmail),
      user
    );

    SendEmailCommand command = SendEmailCommand.of(userCreatedEmailNotification);
    commandBus.dispatch(command);
  }
}


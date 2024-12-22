package com.app.notificarionService.notifications.application.bus.command;

import com.app.notificarionService._shared.bus.command.Command;
import com.app.notificarionService._shared.bus.command.CommandBus;
import com.app.notificarionService.notifications.domain.model.EmailNotification;
import com.app.notificarionService.notifications.domain.valueObject.notification.Email;
import com.app.notificarionService.notifications.domain.valueObject.notification.SubjectEmail;

import java.util.List;

public record SendEmailCommand(List<Email> recipientsEmail, SubjectEmail subject, String htmlBody) implements Command {
  public static SendEmailCommand of(EmailNotification emailNotification){
    return new SendEmailCommand(
      emailNotification.getRecipientsEmail(),
      emailNotification.getSubject(),
      emailNotification.getHtmlBody());
  }
  public void dispatch(CommandBus commandBus) {
  }
}

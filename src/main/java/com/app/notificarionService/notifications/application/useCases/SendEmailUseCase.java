package com.app.notificarionService.notifications.application.useCases;

import com.app.notificarionService.notifications.application.bus.command.SendEmailCommand;
import com.app.notificarionService.notifications.domain.service.EmailService;
import org.springframework.stereotype.Service;

@Service
public class SendEmailUseCase {
  private final EmailService emailService;
  public SendEmailUseCase(EmailService emailService){
    this.emailService = emailService;
  }

  public void execute(SendEmailCommand command){
    emailService.send(command.recipientsEmail(), command.subject(), command.htmlBody());
  }
}

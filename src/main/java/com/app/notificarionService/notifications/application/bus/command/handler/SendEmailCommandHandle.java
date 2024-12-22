package com.app.notificarionService.notifications.application.bus.command.handler;

import com.app.notificarionService._shared.bus.command.CommandHandler;
import com.app.notificarionService.notifications.application.bus.command.SendEmailCommand;
import com.app.notificarionService.notifications.application.useCases.SendEmailUseCase;
import org.springframework.stereotype.Component;

@Component
public class SendEmailCommandHandle implements CommandHandler<SendEmailCommand> {

  private final SendEmailUseCase sendEmailUseCase;
  public SendEmailCommandHandle(SendEmailUseCase sendEmailUseCase){
    this.sendEmailUseCase = sendEmailUseCase;
  }
  @Override
  public void handle(SendEmailCommand sendEmailCommand) {
    sendEmailUseCase.execute(sendEmailCommand);
  }

  @Override
  public Class<SendEmailCommand> getCommandType() {
    return SendEmailCommand.class;
  }
}

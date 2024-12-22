package com.app.notificarionService.notifications.application.bus.command;


import com.app.notificarionService._shared.bus.command.Command;
import com.app.notificarionService._shared.bus.command.CommandBus;
import com.app.notificarionService._shared.bus.command.CommandHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationsCommandBus implements CommandBus {

  private final Map<Class<? extends Command>, CommandHandler> handlers = new HashMap<>();
  private final List<CommandHandler> handlerList;

  public NotificationsCommandBus(List<CommandHandler> handlerList) {
    this.handlerList = handlerList;
  }

  @PostConstruct
  public void registerHandlers() {
    for (CommandHandler handler : handlerList) {
      Class<? extends Command> commandType = handler.getCommandType();
      registerHandler(commandType, handler);
    }
  }

  public <T extends Command> void registerHandler(Class<T> commandType, CommandHandler<T> handler) {
    handlers.put(commandType, handler);
  }

  @Override
  public void dispatch(Command command) {
    CommandHandler handler = handlers.get(command.getClass());
    if (handler != null) {
      handler.handle(command);
    } else {
      throw new IllegalArgumentException("No handler user found for command: " + command.getClass().getName());
    }
  }
}

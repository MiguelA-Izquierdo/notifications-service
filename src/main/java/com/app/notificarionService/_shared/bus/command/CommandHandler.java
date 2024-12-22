package com.app.notificarionService._shared.bus.command;

public interface CommandHandler<T extends Command> {
  void handle(T command);
  Class<T> getCommandType();
}

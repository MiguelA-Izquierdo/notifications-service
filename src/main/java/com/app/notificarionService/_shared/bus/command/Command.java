package com.app.notificarionService._shared.bus.command;

public interface Command {
  void dispatch(CommandBus commandBus);
}

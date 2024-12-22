package com.app.notificarionService._shared.bus.command;

public interface CommandBus {
  void dispatch(Command command);
}

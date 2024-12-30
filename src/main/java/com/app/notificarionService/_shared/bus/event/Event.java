package com.app.notificarionService._shared.bus.event;

public interface Event<T> {
  String getQueue();

  String getExchange();

  String getRoutingKey();
  T getPayload();
}

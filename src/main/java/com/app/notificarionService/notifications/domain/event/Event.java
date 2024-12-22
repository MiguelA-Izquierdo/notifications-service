package com.app.notificarionService.notifications.domain.event;

public interface Event<T> {
  String getQueue();

  String getExchange();

  String getRoutingKey();
  T getPayload();
}

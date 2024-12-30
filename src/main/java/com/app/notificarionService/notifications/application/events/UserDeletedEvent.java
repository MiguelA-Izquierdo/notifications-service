package com.app.notificarionService.notifications.application.events;

import com.app.notificarionService._shared.bus.event.Event;

import java.util.UUID;

public class UserDeletedEvent implements Event<UserDeletedEvent.UserPayload> {

  private final String userExchange;
  private final String userDeletedQueue;
  private final String userDeletedRoutingKey;
  private final UserPayload payload;

  public UserDeletedEvent(String userExchange, String userDeletedQueue, String userDeletedRoutingKey, UserPayload payload) {
    this.userExchange = userExchange;
    this.userDeletedQueue = userDeletedQueue;
    this.userDeletedRoutingKey = userDeletedRoutingKey;
    this.payload = payload;
  }

  @Override
  public String getQueue() {
    return userDeletedQueue;
  }

  @Override
  public String getExchange() {
    return userExchange;
  }

  @Override
  public String getRoutingKey() {
    return userDeletedRoutingKey;
  }

  @Override
  public UserPayload getPayload() {
    return this.payload;
  }

  public record UserPayload(UUID userId, String name, String lastName, String email) {}
}

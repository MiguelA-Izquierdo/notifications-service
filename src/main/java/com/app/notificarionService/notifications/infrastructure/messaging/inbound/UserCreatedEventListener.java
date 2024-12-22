package com.app.notificarionService.notifications.infrastructure.messaging.inbound;

import com.app.notificarionService.notifications.application.events.handlers.UserCreatedEventHandler;
import com.app.notificarionService.notifications.application.events.UserCreatedEvent;
import com.app.notificarionService.notifications.infrastructure.serialization.JsonSerializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedEventListener {

  private static final Logger logger = LoggerFactory.getLogger(UserCreatedEventListener.class);
  private final UserCreatedEventHandler eventHandler;
  private final JsonSerializationService jsonSerializationService;

  public UserCreatedEventListener(JsonSerializationService jsonSerializationService,
                                  UserCreatedEventHandler eventHandler) {
    this.jsonSerializationService = jsonSerializationService;
    this.eventHandler = eventHandler;
  }

  @RabbitListener(queues = "${messaging.queue.user.created}")
  public void onEvent(String jsonMessage) {
    logger.info("Received JSON message: {}", jsonMessage);

    try {
      UserCreatedEvent event = jsonSerializationService.deserializeEvent(
        jsonMessage,
        UserCreatedEvent.class,
        UserCreatedEvent.UserPayload.class
      );
      eventHandler.handle(event);
    } catch (Exception e) {
      logger.error("Error processing event", e);
      throw new RuntimeException("Error processing event", e);
    }
  }
}

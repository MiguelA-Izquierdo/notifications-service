package com.app.notificarionService.notifications.infrastructure.messaging.inbound;

import com.app.notificarionService.notifications.application.events.UserDeletedEvent;
import com.app.notificarionService.notifications.application.events.handlers.UserDeletedEventHandler;
import com.app.notificarionService.notifications.infrastructure.serialization.JsonSerializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserDeletedEventListener {
  private static final Logger logger = LoggerFactory.getLogger(UserDeletedEventListener.class);
  private final UserDeletedEventHandler eventHandler;
  private final JsonSerializationService jsonSerializationService;

  public UserDeletedEventListener(JsonSerializationService jsonSerializationService,
                                  UserDeletedEventHandler eventHandler) {
    this.jsonSerializationService = jsonSerializationService;
    this.eventHandler = eventHandler;
  }

  @RabbitListener(queues = "${messaging.queue.user.deleted}")
  public void onEvent(String jsonMessage) {
    logger.info("Received JSON message: {}", jsonMessage);

    try {
      UserDeletedEvent event = jsonSerializationService.deserializeEvent(
        jsonMessage,
        UserDeletedEvent.class,
        UserDeletedEvent.UserPayload.class
      );
      eventHandler.handle(event);
    } catch (Exception e) {
      logger.error("Error processing event", e);
      throw new RuntimeException("Error processing event", e);
    }
  }
}

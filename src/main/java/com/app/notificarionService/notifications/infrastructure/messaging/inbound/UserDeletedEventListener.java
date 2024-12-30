package com.app.notificarionService.notifications.infrastructure.messaging.inbound;

import com.app.notificarionService.notifications.application.events.UserDeletedEvent;
import com.app.notificarionService.notifications.application.events.handlers.UserDeletedEventHandler;
import com.app.notificarionService.notifications.infrastructure.serialization.JsonSerializationService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

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

  @RabbitListener(queues = "${messaging.queue.userDeleted}")
  public void onEvent(Message message, Channel channel) {
    String bodyMessage = new String(message.getBody(), StandardCharsets.UTF_8);
    try {
      UserDeletedEvent event = jsonSerializationService.deserializeEvent(
        bodyMessage,
        UserDeletedEvent.class,
        UserDeletedEvent.UserPayload.class
      );
      eventHandler.handle(event);

      channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
      logger.info("Mensaje procesado y confirmado (ack)");
    } catch (Exception e) {
      logger.error("Error processing event", e);
      try {
        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        logger.info("Mensaje marcado como no procesado (nack), se reintentará.");
      } catch (Exception nackException) {
        logger.error("Error al hacer nack del mensaje", nackException);
      }
    }
  }
}

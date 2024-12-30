package com.app.notificarionService.notifications.infrastructure.messaging.inbound;

import com.app.notificarionService.notifications.application.events.handlers.UserCreatedEventHandler;
import com.app.notificarionService.notifications.application.events.UserCreatedEvent;
import com.app.notificarionService.notifications.infrastructure.serialization.JsonSerializationService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

  @RabbitListener(queues = "${messaging.queue.userCreated}", ackMode = "MANUAL")
  public void onEvent(Message message, Channel channel) {
    long deliveryTag = message.getMessageProperties().getDeliveryTag();

    try {
      eventHandler.handle(getEventFromMessage(message)).join();

      channel.basicAck(deliveryTag, false);
      logger.info("Mensaje procesado y confirmado (ack): {}", deliveryTag);

    } catch (Exception ex) {
      try {
        channel.basicNack(deliveryTag, false, true);
        logger.warn("Mensaje marcado como no procesado (nack): {}", deliveryTag);
      } catch (IOException nackException) {
        logger.error("Error al manejar el nack para el mensaje", nackException);
      }
      logger.error("Error en el procesamiento del mensaje: {}", deliveryTag, ex);
    }
  }



  private UserCreatedEvent getEventFromMessage(Message message) {
    String serializedEvent = new String(message.getBody(), StandardCharsets.UTF_8);
    return jsonSerializationService.deserializeEvent(serializedEvent, UserCreatedEvent.class, UserCreatedEvent.UserPayload.class);
  }


}

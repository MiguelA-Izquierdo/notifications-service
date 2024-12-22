package com.app.notificarionService.notifications.infrastructure.serialization;

import com.app.notificarionService.notifications.domain.event.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class JsonSerializationService {

  private final ObjectMapper objectMapper;

  public JsonSerializationService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String serialize(Object object) throws JsonProcessingException {
    return objectMapper.writeValueAsString(object);
  }

  public <T extends Event<P>, P> T deserializeEvent(String jsonMessage, Class<T> eventClass, Class<P> payloadClass) {
    try {
      JsonNode rootNode = objectMapper.readTree(jsonMessage);

      String exchange = rootNode.get("exchange").asText();
      String queue = rootNode.get("queue").asText();
      String routingKey = rootNode.get("routingKey").asText();

      String payloadJson = rootNode.get("payload").asText();
      P payload = objectMapper.readValue(payloadJson, payloadClass);

      return eventClass.getDeclaredConstructor(String.class, String.class, String.class, payloadClass)
        .newInstance(exchange, queue, routingKey, payload);
    } catch (Exception e) {
      throw new RuntimeException("Error al deserializar el evento", e);
    }
  }

}

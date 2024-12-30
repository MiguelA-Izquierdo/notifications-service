package com.app.notificarionService.notifications.infrastructure.serialization;

import com.app.notificarionService._shared.bus.event.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JsonSerializationService {
  private static final Logger logger = LoggerFactory.getLogger(JsonSerializationService.class);

  private final ObjectMapper objectMapper;

  public JsonSerializationService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String serialize(Object object) throws JsonProcessingException {
    return objectMapper.writeValueAsString(object);
  }

  public <T extends Event<P>, P> T deserializeEvent(String rawJson, Class<T> eventClass, Class<P> payloadClass) {

    try {
      String jsonMessage = normalizeJson(rawJson);
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

  private String normalizeJson(String rawJson) {
    try {
      String normalizedJson = rawJson.replace("\\\"", "\"")
        .replace("\\\\", "\\");

      if (normalizedJson.startsWith("\"") && normalizedJson.endsWith("\"")) {
        normalizedJson = normalizedJson.substring(1, normalizedJson.length() - 1);
      }

      return normalizedJson;
    } catch (Exception e) {
      logger.error("Error normalizing JSON", e);
      throw new RuntimeException("Error normalizing JSON", e);
    }
  }






}

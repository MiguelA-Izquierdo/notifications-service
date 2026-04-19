package com.app.notificationService.notifications.infrastructure.serialization;

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

    /**
     * Extracts and deserializes the {@code payload} field from an event envelope JSON.
     *
     * <p>Expected envelope format (produced by the upstream service):
     * <pre>
     * {
     *   "exchange":   "userExchange",
     *   "queue":      "userCreatedQueue",
     *   "routingKey": "user.created",
     *   "payload":    "{\"userId\":\"...\", \"name\":\"...\", ...}"
     * }
     * </pre>
     *
     * <p>The outer envelope may arrive double-escaped from the broker; {@link #normalizeJson}
     * handles that transparently. The {@code payload} value may be a JSON string literal
     * or an embedded JSON object — both forms are accepted.
     *
     * @param rawJson      raw message body as received from the broker
     * @param payloadClass target class for the payload
     * @param <P>          payload type
     * @return deserialized payload instance
     * @throws RuntimeException if the JSON cannot be parsed or the payload field is missing
     */
    public <P> P deserializePayload(String rawJson, Class<P> payloadClass) {
        try {
            String json = normalizeJson(rawJson);
            JsonNode root = objectMapper.readTree(json);

            JsonNode payloadNode = root.get("payload");
            if (payloadNode == null) {
                throw new IllegalArgumentException("Missing 'payload' field in message envelope");
            }

            // payload is a JSON string literal when the producer serializes it twice
            String payloadJson = payloadNode.isTextual() ? payloadNode.asText() : payloadNode.toString();
            return objectMapper.readValue(payloadJson, payloadClass);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Mensaje rechazado: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error al deserializar el payload del evento", e);
        }
    }

    /**
     * Normalises double-encoded JSON produced by the upstream service.
     *
     * <p>Some producers serialise the message twice: the full JSON object is turned into
     * a JSON string, so the broker receives {@code "{\"exchange\":\"...\"}"}
     * instead of {@code {"exchange":"..."}}. When the body starts and ends with a
     * {@code "}, this method strips that outer layer and unescapes the inner quotes.
     *
     * <p>Standard (single-encoded) JSON is returned unchanged.
     */
    private String normalizeJson(String rawJson) {
        String trimmed = rawJson.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return trimmed;
    }
}

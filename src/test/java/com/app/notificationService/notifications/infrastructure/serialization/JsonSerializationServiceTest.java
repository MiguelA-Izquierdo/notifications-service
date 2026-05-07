package com.app.notificationService.notifications.infrastructure.serialization;

import com.app.notificationService.notifications.application.events.UserCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonSerializationServiceTest {

    private JsonSerializationService service;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // Mirror the production JacksonConfig
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        service = new JsonSerializationService(mapper);
    }

    @Test
    void shouldDeserializeValidPayload() {
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        String json = """
                {
                  "exchange": "userExchange",
                  "queue": "userCreatedQueue",
                  "routingKey": "user.created",
                  "payload": "{\\"userId\\":\\"550e8400-e29b-41d4-a716-446655440000\\",\\"name\\":\\"John\\",\\"lastName\\":\\"Doe\\",\\"email\\":\\"john@example.com\\"}"
                }
                """;

        UserCreatedEvent.UserPayload payload = service.deserializePayload(json, UserCreatedEvent.UserPayload.class);

        assertThat(payload.userId()).isEqualTo(userId);
        assertThat(payload.name()).isEqualTo("John");
        assertThat(payload.lastName()).isEqualTo("Doe");
        assertThat(payload.email()).isEqualTo("john@example.com");
    }

    @Test
    void shouldDeserializeOuterQuoteWrappedJson() throws JsonProcessingException {
        // Some producers serialise the message body twice: the envelope JSON object is
        // turned into a string and that string is then JSON-encoded again, producing a
        // body that starts and ends with a double-quote character.
        // normalizeJson() strips that outer layer before parsing.
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        // Step 1: build the inner payload JSON
        String payloadJson = mapper.writeValueAsString(
                Map.of("userId", userId.toString(), "name", "Jane", "lastName", "Doe", "email", "jane@example.com")
        );

        // Step 2: build the standard envelope with the payload as a string value
        String envelopeJson = mapper.writeValueAsString(
                Map.of("exchange", "userExchange", "queue", "userCreatedQueue",
                       "routingKey", "user.created", "payload", payloadJson)
        );

        // Step 3: double-encode — serialise the whole envelope as a JSON string
        String doubleEncoded = mapper.writeValueAsString(envelopeJson);

        UserCreatedEvent.UserPayload payload = service.deserializePayload(doubleEncoded, UserCreatedEvent.UserPayload.class);

        assertThat(payload.userId()).isEqualTo(userId);
        assertThat(payload.name()).isEqualTo("Jane");
    }

    @Test
    void shouldThrowWhenPayloadFieldIsMissing() {
        String json = """
                {
                  "exchange": "userExchange",
                  "queue": "userCreatedQueue",
                  "routingKey": "user.created"
                }
                """;

        assertThatThrownBy(() -> service.deserializePayload(json, UserCreatedEvent.UserPayload.class))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("payload");
    }

    @Test
    void shouldExtractEventIdFromEnvelope() {
        String json = """
                {
                  "eventId": "550e8400-e29b-41d4-a716-446655440000",
                  "exchange": "userExchange",
                  "routingKey": "user.created",
                  "payload": "{\\"userId\\":\\"550e8400-e29b-41d4-a716-446655440000\\"}"
                }
                """;

        assertThat(service.extractEventId(json))
                .isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    void shouldReturnNullWhenEventIdIsMissing() {
        String json = """
                {
                  "exchange": "userExchange",
                  "payload": "{\\"userId\\":\\"550e8400-e29b-41d4-a716-446655440000\\"}"
                }
                """;

        assertThat(service.extractEventId(json)).isNull();
    }

    @Test
    void shouldReturnNullWhenEventIdIsInvalidUuid() {
        String json = """
                {
                  "eventId": "not-a-uuid",
                  "payload": "{}"
                }
                """;

        assertThat(service.extractEventId(json)).isNull();
    }

    @Test
    void shouldThrowWhenJsonIsMalformed() {
        String malformed = "{ not valid json }";

        assertThatThrownBy(() -> service.deserializePayload(malformed, UserCreatedEvent.UserPayload.class))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldThrowWhenPayloadDoesNotMatchTargetType() {
        String json = """
                {
                  "exchange": "userExchange",
                  "queue": "userCreatedQueue",
                  "routingKey": "user.created",
                  "payload": "{\\"unexpected\\":\\"fields\\"}"
                }
                """;

        // Jackson maps unknown fields leniently by default; unknown properties produce null fields.
        // The payload object is constructed — it just has null fields.
        UserCreatedEvent.UserPayload payload = service.deserializePayload(json, UserCreatedEvent.UserPayload.class);
        assertThat(payload.userId()).isNull();
        assertThat(payload.name()).isNull();
    }
}
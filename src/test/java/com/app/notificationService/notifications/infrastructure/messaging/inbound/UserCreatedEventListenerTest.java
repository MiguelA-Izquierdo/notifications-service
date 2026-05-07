package com.app.notificationService.notifications.infrastructure.messaging.inbound;

import com.app.notificationService.notifications.application.events.UserCreatedEvent;
import com.app.notificationService.notifications.application.events.handlers.UserCreatedEventHandler;
import com.app.notificationService.notifications.infrastructure.serialization.JsonSerializationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCreatedEventListenerTest {

    @Mock
    private JsonSerializationService jsonSerializationService;

    @Mock
    private UserCreatedEventHandler eventHandler;

    @InjectMocks
    private UserCreatedEventListener listener;

    private static final String JSON = "{\"userId\":\"00000000-0000-0000-0000-000000000001\",\"name\":\"Miguel\",\"lastName\":\"Izquierdo\",\"email\":\"miguel@example.com\"}";

    private Message buildMessage() {
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(1L);
        return new Message(JSON.getBytes(), props);
    }

    private UserCreatedEvent.UserPayload buildPayload() {
        return new UserCreatedEvent.UserPayload(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Miguel", "Izquierdo", "miguel@example.com"
        );
    }

    @Test
    void shouldDeserializeToUserCreatedEvent() {
        when(jsonSerializationService.deserializePayload(JSON, UserCreatedEvent.UserPayload.class))
                .thenReturn(buildPayload());

        UserCreatedEvent event = listener.deserialize(buildMessage());

        assertThat(event.getPayload().name()).isEqualTo("Miguel");
        assertThat(event.getPayload().email()).isEqualTo("miguel@example.com");
    }

    @Test
    void shouldDelegateToHandler() {
        UserCreatedEvent event = new UserCreatedEvent(null, buildPayload());

        listener.process(event);

        ArgumentCaptor<UserCreatedEvent> captor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventHandler).handle(captor.capture());
        assertThat(captor.getValue()).isSameAs(event);
    }

    @Test
    void shouldCallDeserializePayloadWithCorrectClass() {
        when(jsonSerializationService.deserializePayload(eq(JSON), eq(UserCreatedEvent.UserPayload.class)))
                .thenReturn(buildPayload());

        listener.deserialize(buildMessage());

        verify(jsonSerializationService).deserializePayload(JSON, UserCreatedEvent.UserPayload.class);
    }
}
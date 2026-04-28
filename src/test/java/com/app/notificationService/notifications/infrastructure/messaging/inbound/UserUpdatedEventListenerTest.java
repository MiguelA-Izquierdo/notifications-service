package com.app.notificationService.notifications.infrastructure.messaging.inbound;

import com.app.notificationService.notifications.application.events.UserUpdatedEvent;
import com.app.notificationService.notifications.application.events.handlers.UserUpdatedEventHandler;
import com.app.notificationService.notifications.infrastructure.serialization.JsonSerializationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUpdatedEventListenerTest {

    @Mock
    private JsonSerializationService jsonSerializationService;

    @Mock
    private UserUpdatedEventHandler eventHandler;

    @InjectMocks
    private UserUpdatedEventListener listener;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String JSON = "{\"userId\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"name\":{\"oldValue\":\"Miguel\",\"newValue\":\"Mike\"}}}";

    private Message buildMessage() {
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(1L);
        return new Message(JSON.getBytes(), props);
    }

    private UserUpdatedEvent.UserPayload buildPayload() {
        return new UserUpdatedEvent.UserPayload(
            USER_ID,
            Map.of("name", new UserUpdatedEvent.UserPayload.FieldChange("Miguel", "Mike"))
        );
    }

    @Test
    void shouldDeserializeToUserUpdatedEvent() {
        when(jsonSerializationService.deserializePayload(JSON, UserUpdatedEvent.UserPayload.class))
            .thenReturn(buildPayload());

        UserUpdatedEvent event = listener.deserialize(buildMessage());

        assertThat(event.getPayload().userId()).isEqualTo(USER_ID);
        assertThat(event.getPayload().changes()).containsKey("name");
    }

    @Test
    void shouldDelegateToHandler() {
        UserUpdatedEvent event = new UserUpdatedEvent(buildPayload());

        listener.process(event);

        ArgumentCaptor<UserUpdatedEvent> captor = ArgumentCaptor.forClass(UserUpdatedEvent.class);
        verify(eventHandler).handle(captor.capture());
        assertThat(captor.getValue()).isSameAs(event);
    }

    @Test
    void shouldCallDeserializePayloadWithCorrectClass() {
        when(jsonSerializationService.deserializePayload(eq(JSON), eq(UserUpdatedEvent.UserPayload.class)))
            .thenReturn(buildPayload());

        listener.deserialize(buildMessage());

        verify(jsonSerializationService).deserializePayload(JSON, UserUpdatedEvent.UserPayload.class);
    }
}
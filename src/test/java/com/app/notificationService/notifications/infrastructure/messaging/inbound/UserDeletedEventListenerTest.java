package com.app.notificationService.notifications.infrastructure.messaging.inbound;

import com.app.notificationService.notifications.application.events.UserDeletedEvent;
import com.app.notificationService.notifications.application.events.handlers.UserDeletedEventHandler;
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
class UserDeletedEventListenerTest {

    @Mock
    private JsonSerializationService jsonSerializationService;

    @Mock
    private UserDeletedEventHandler eventHandler;

    @InjectMocks
    private UserDeletedEventListener listener;

    private static final String JSON = "{\"userId\":\"00000000-0000-0000-0000-000000000002\",\"name\":\"Ana\",\"lastName\":\"García\",\"email\":\"ana@example.com\"}";

    private Message buildMessage() {
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(2L);
        return new Message(JSON.getBytes(), props);
    }

    private UserDeletedEvent.UserPayload buildPayload() {
        return new UserDeletedEvent.UserPayload(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "Ana", "García", "ana@example.com"
        );
    }

    @Test
    void shouldDeserializeToUserDeletedEvent() {
        when(jsonSerializationService.deserializePayload(JSON, UserDeletedEvent.UserPayload.class))
                .thenReturn(buildPayload());

        UserDeletedEvent event = listener.deserialize(buildMessage());

        assertThat(event.getPayload().name()).isEqualTo("Ana");
        assertThat(event.getPayload().email()).isEqualTo("ana@example.com");
    }

    @Test
    void shouldDelegateToHandler() {
        UserDeletedEvent event = new UserDeletedEvent(buildPayload());

        listener.process(event);

        ArgumentCaptor<UserDeletedEvent> captor = ArgumentCaptor.forClass(UserDeletedEvent.class);
        verify(eventHandler).handle(captor.capture());
        assertThat(captor.getValue()).isSameAs(event);
    }

    @Test
    void shouldCallDeserializePayloadWithCorrectClass() {
        when(jsonSerializationService.deserializePayload(eq(JSON), eq(UserDeletedEvent.UserPayload.class)))
                .thenReturn(buildPayload());

        listener.deserialize(buildMessage());

        verify(jsonSerializationService).deserializePayload(JSON, UserDeletedEvent.UserPayload.class);
    }
}
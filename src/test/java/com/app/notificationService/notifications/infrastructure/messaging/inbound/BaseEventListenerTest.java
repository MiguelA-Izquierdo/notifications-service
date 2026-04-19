package com.app.notificationService.notifications.infrastructure.messaging.inbound;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseEventListenerTest {

    @Mock
    private Channel channel;

    private static final long DELIVERY_TAG = 42L;

    private Message buildMessage() {
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(DELIVERY_TAG);
        return new Message("{}".getBytes(), props);
    }

    @Test
    void shouldAckWhenProcessingSucceeds() throws IOException {
        BaseEventListener<String> listener = new BaseEventListener<>() {
            @Override
            protected String deserialize(Message message) {
                return "event";
            }

            @Override
            protected void process(String event) {
                // no-op — success
            }
        };

        listener.handleMessage(buildMessage(), channel);

        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void shouldNackToDlqWhenDeserializationFails() throws IOException {
        BaseEventListener<String> listener = new BaseEventListener<>() {
            @Override
            protected String deserialize(Message message) {
                throw new RuntimeException("bad JSON");
            }

            @Override
            protected void process(String event) {}
        };

        listener.handleMessage(buildMessage(), channel);

        verify(channel).basicNack(DELIVERY_TAG, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void shouldNackToDlqWhenProcessingFails() throws IOException {
        BaseEventListener<String> listener = new BaseEventListener<>() {
            @Override
            protected String deserialize(Message message) {
                return "event";
            }

            @Override
            protected void process(String event) {
                throw new RuntimeException("SMTP error");
            }
        };

        listener.handleMessage(buildMessage(), channel);

        verify(channel).basicNack(DELIVERY_TAG, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void shouldNotRequeueOnNack() throws IOException {
        BaseEventListener<String> listener = new BaseEventListener<>() {
            @Override
            protected String deserialize(Message message) {
                throw new RuntimeException("permanent error");
            }

            @Override
            protected void process(String event) {}
        };

        listener.handleMessage(buildMessage(), channel);

        // third arg is `requeue` — must be false to route to DLQ
        verify(channel).basicNack(DELIVERY_TAG, false, false);
    }
}
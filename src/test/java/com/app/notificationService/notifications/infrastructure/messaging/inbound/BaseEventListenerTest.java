package com.app.notificationService.notifications.infrastructure.messaging.inbound;

import com.app.notificationService._shared.domain.bus.event.Event;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class BaseEventListenerTest {

    @Mock private Channel channel;
    @Mock private RabbitTemplate rabbitTemplate;

    private static final long DELIVERY_TAG = 42L;
    private static final String RETRY_EXCHANGE = "user.retry.exchange";
    private static final String CONSUMER_QUEUE = "userCreatedQueue";

    private record TestEvent(UUID eventId) implements Event<Void> {
        @Override public Void getPayload() { return null; }
        @Override public UUID getEventId() { return eventId; }
    }

    private Message buildMessage() {
        return buildMessage(0);
    }

    private Message buildMessage(int retryCount) {
        return buildMessageWithBody("{}".getBytes(), retryCount);
    }

    private Message buildMessageWithBody(byte[] body) {
        return buildMessageWithBody(body, 0);
    }

    private Message buildMessageWithBody(byte[] body, int retryCount) {
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(DELIVERY_TAG);
        props.setConsumerQueue(CONSUMER_QUEUE);
        if (retryCount > 0) {
            props.getHeaders().put("x-retry-count", retryCount);
        }
        return new Message(body, props);
    }

    private BaseEventListener<TestEvent> listenerThatSucceeds() {
        return new BaseEventListener<>(rabbitTemplate, RETRY_EXCHANGE, null) {
            @Override protected TestEvent deserialize(Message message) { return new TestEvent(null); }
            @Override protected void process(TestEvent event) {}
        };
    }

    private BaseEventListener<TestEvent> listenerThatFailsDeserialization() {
        return new BaseEventListener<>(rabbitTemplate, RETRY_EXCHANGE, null) {
            @Override protected TestEvent deserialize(Message message) { throw new RuntimeException("bad JSON"); }
            @Override protected void process(TestEvent event) {}
        };
    }

    private BaseEventListener<TestEvent> listenerThatFailsProcessing() {
        return new BaseEventListener<>(rabbitTemplate, RETRY_EXCHANGE, null) {
            @Override protected TestEvent deserialize(Message message) { return new TestEvent(null); }
            @Override protected void process(TestEvent event) { throw new RuntimeException("SMTP error"); }
        };
    }

    @Test
    void shouldAckWhenProcessingSucceeds() throws IOException {
        listenerThatSucceeds().handleMessage(buildMessage(), channel);

        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void shouldNackToDlqWhenDeserializationFails() throws IOException {
        listenerThatFailsDeserialization().handleMessage(buildMessage(), channel);

        verify(channel).basicNack(DELIVERY_TAG, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void shouldNotRequeueOnDeserializationNack() throws IOException {
        listenerThatFailsDeserialization().handleMessage(buildMessage(), channel);

        verify(channel).basicNack(DELIVERY_TAG, false, false);
    }

    @Test
    void shouldScheduleFirstRetryWhenProcessingFails() throws IOException {
        listenerThatFailsProcessing().handleMessage(buildMessage(), channel);

        verify(rabbitTemplate).send(eq(RETRY_EXCHANGE), eq(CONSUMER_QUEUE + ".retry.30s"), any(Message.class));
        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void shouldEscalateToSecondRetryLevel() throws IOException {
        listenerThatFailsProcessing().handleMessage(buildMessage(1), channel);

        verify(rabbitTemplate).send(eq(RETRY_EXCHANGE), eq(CONSUMER_QUEUE + ".retry.2min"), any(Message.class));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    void shouldEscalateToThirdRetryLevel() throws IOException {
        listenerThatFailsProcessing().handleMessage(buildMessage(2), channel);

        verify(rabbitTemplate).send(eq(RETRY_EXCHANGE), eq(CONSUMER_QUEUE + ".retry.10min"), any(Message.class));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    void shouldSendToParkingWhenRetriesExhausted() throws IOException {
        listenerThatFailsProcessing().handleMessage(buildMessage(3), channel);

        verify(rabbitTemplate).send(eq(RETRY_EXCHANGE), eq(CONSUMER_QUEUE + ".parking"), any(Message.class));
        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void shouldNackToDlqWhenRetryPublishFails() throws IOException {
        doThrow(new RuntimeException("broker down")).when(rabbitTemplate).send(any(String.class), any(String.class), any(Message.class));

        listenerThatFailsProcessing().handleMessage(buildMessage(), channel);

        verify(channel).basicNack(DELIVERY_TAG, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void shouldSkipDuplicateEventAndAck() throws IOException {
        UUID eventId = UUID.randomUUID();
        ProcessedMessageStore store = new ProcessedMessageStore(24L, 10_000);
        AtomicInteger processCount = new AtomicInteger(0);
        BaseEventListener<TestEvent> listener = new BaseEventListener<>(rabbitTemplate, RETRY_EXCHANGE, store) {
            @Override protected TestEvent deserialize(Message message) { return new TestEvent(eventId); }
            @Override protected void process(TestEvent event) { processCount.incrementAndGet(); }
        };

        listener.handleMessage(buildMessage(), channel);
        listener.handleMessage(buildMessage(), channel);

        verify(channel, times(2)).basicAck(DELIVERY_TAG, false);
        assertThat(processCount.get()).isOne();
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void shouldMarkEventAsProcessedAfterSuccess() throws IOException {
        UUID eventId = UUID.randomUUID();
        ProcessedMessageStore store = new ProcessedMessageStore(24L, 10_000);
        AtomicInteger processCount = new AtomicInteger(0);
        BaseEventListener<TestEvent> listener = new BaseEventListener<>(rabbitTemplate, RETRY_EXCHANGE, store) {
            @Override protected TestEvent deserialize(Message message) { return new TestEvent(eventId); }
            @Override protected void process(TestEvent event) { processCount.incrementAndGet(); }
        };

        listener.handleMessage(buildMessage(), channel);
        listener.handleMessage(buildMessage(), channel);

        assertThat(processCount.get()).isOne();
    }

    @Test
    void shouldProcessAgainWhenSameEventIdArrivesWithDifferentPayload() throws IOException {
        UUID eventId = UUID.randomUUID();
        ProcessedMessageStore store = new ProcessedMessageStore(24L, 10_000);
        AtomicInteger processCount = new AtomicInteger(0);
        BaseEventListener<TestEvent> listener = new BaseEventListener<>(rabbitTemplate, RETRY_EXCHANGE, store) {
            @Override protected TestEvent deserialize(Message message) { return new TestEvent(eventId); }
            @Override protected void process(TestEvent event) { processCount.incrementAndGet(); }
        };

        listener.handleMessage(buildMessageWithBody("{\"v\":1}".getBytes()), channel);
        listener.handleMessage(buildMessageWithBody("{\"v\":2}".getBytes()), channel);

        assertThat(processCount.get()).isEqualTo(2);
    }

    @Test
    void shouldProcessNormallyWhenEventIdIsNull() throws IOException {
        ProcessedMessageStore store = new ProcessedMessageStore(24L, 10_000);
        AtomicInteger processCount = new AtomicInteger(0);
        BaseEventListener<TestEvent> listener = new BaseEventListener<>(rabbitTemplate, RETRY_EXCHANGE, store) {
            @Override protected TestEvent deserialize(Message message) { return new TestEvent(null); }
            @Override protected void process(TestEvent event) { processCount.incrementAndGet(); }
        };

        listener.handleMessage(buildMessage(), channel);

        verify(channel).basicAck(DELIVERY_TAG, false);
        assertThat(processCount.get()).isOne();
    }
}
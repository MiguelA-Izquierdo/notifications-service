package com.app.notificationService.notifications.infrastructure.messaging.inbound;

import com.app.notificationService._shared.domain.bus.event.Event;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseEventListener<T extends Event<?>> {

    private static final Logger logger = LoggerFactory.getLogger(BaseEventListener.class);

    private static final String RETRY_COUNT_HEADER = "x-retry-count";
    private static final int MAX_RETRIES = 3;
    private static final String[] RETRY_SUFFIXES = {".retry.30s", ".retry.2min", ".retry.10min"};

    private final RabbitTemplate rabbitTemplate;
    private final String retryExchange;
    private final ProcessedMessageStore processedMessageStore;

    protected BaseEventListener(RabbitTemplate rabbitTemplate,
                                 String retryExchange,
                                 ProcessedMessageStore processedMessageStore) {
        this.rabbitTemplate = rabbitTemplate;
        this.retryExchange = retryExchange;
        this.processedMessageStore = processedMessageStore;
    }

    protected abstract T deserialize(Message message);

    protected abstract void process(T event);

    protected void handleMessage(Message message, Channel channel) {
        long tag = message.getMessageProperties().getDeliveryTag();

        T event;
        try {
            event = deserialize(message);
        } catch (Exception ex) {
            logger.error("Error deserializando mensaje (delivery tag {}): {}", tag, ex.getMessage(), ex);
            nackToDlq(channel, tag);
            return;
        }

        UUID eventId = event.getEventId();
        String contentHash = contentHash(message);
        if (eventId != null && processedMessageStore != null && processedMessageStore.isAlreadyProcessed(eventId.toString(), contentHash)) {
            logger.warn("Evento duplicado ignorado (eventId={}, delivery tag={})", eventId, tag);
            ackQuietly(channel, tag);
            return;
        }

        try {
            process(event);
            channel.basicAck(tag, false);
            if (eventId != null && processedMessageStore != null) {
                processedMessageStore.markAsProcessed(eventId.toString(), contentHash);
            }
            logger.info("ACK: delivery tag {}", tag);
        } catch (Exception ex) {
            logger.error("Error procesando mensaje (delivery tag {}): {}", tag, ex.getMessage(), ex);
            scheduleRetry(message, channel, tag);
        }
    }

    private void scheduleRetry(Message message, Channel channel, long tag) {
        int retryCount = getRetryCount(message);
        String consumerQueue = message.getMessageProperties().getConsumerQueue();

        if (consumerQueue == null) {
            logger.error("consumerQueue nulo — no se puede programar reintento (delivery tag {})", tag);
            nackToDlq(channel, tag);
            return;
        }

        try {
            if (retryCount < MAX_RETRIES) {
                String routingKey = consumerQueue + RETRY_SUFFIXES[retryCount];
                Message retryMessage = buildRetryMessage(message, retryCount + 1);
                rabbitTemplate.send(retryExchange, routingKey, retryMessage);
                channel.basicAck(tag, false);
                logger.warn("Reintento {}/{} programado → {} (delivery tag {})",
                        retryCount + 1, MAX_RETRIES, routingKey, tag);
            } else {
                String parkingKey = consumerQueue + ".parking";
                rabbitTemplate.send(retryExchange, parkingKey, message);
                channel.basicAck(tag, false);
                logger.error("Reintentos agotados, mensaje aparcado → {} (delivery tag {})", parkingKey, tag);
            }
        } catch (Exception ex) {
            logger.error("Error programando reintento (delivery tag {}): {}", tag, ex.getMessage(), ex);
            nackToDlq(channel, tag);
        }
    }

    private int getRetryCount(Message message) {
        Object count = message.getMessageProperties().getHeaders().get(RETRY_COUNT_HEADER);
        return count instanceof Integer i ? i : 0;
    }

    private Message buildRetryMessage(Message original, int newRetryCount) {
        MessageProperties props = original.getMessageProperties();
        props.getHeaders().put(RETRY_COUNT_HEADER, newRetryCount);
        return new Message(original.getBody(), props);
    }

    private void ackQuietly(Channel channel, long tag) {
        try {
            channel.basicAck(tag, false);
        } catch (IOException e) {
            logger.error("Error enviando ACK para evento duplicado (delivery tag {}): {}", tag, e.getMessage(), e);
        }
    }

    private void nackToDlq(Channel channel, long tag) {
        try {
            channel.basicNack(tag, false, false);
            logger.warn("NACK (DLQ): mensaje enviado a dead-letter queue. Delivery tag: {}", tag);
        } catch (IOException e) {
            logger.error("Error enviando NACK para delivery tag {}: {}", tag, e.getMessage(), e);
        }
    }

    private String contentHash(Message message) {
        try {
            String routingKey = Objects.toString(message.getMessageProperties().getReceivedRoutingKey(), "");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(routingKey.getBytes(StandardCharsets.UTF_8));
            digest.update(message.getBody());
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
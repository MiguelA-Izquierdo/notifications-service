package com.app.notificationService.notifications.infrastructure.messaging.inbound;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;

import java.io.IOException;

public abstract class BaseEventListener<T> {

    private static final Logger logger = LoggerFactory.getLogger(BaseEventListener.class);

    protected abstract T deserialize(Message message);

    protected abstract void process(T event);

    protected void handleMessage(Message message, Channel channel) {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            process(deserialize(message));
            channel.basicAck(tag, false);
            logger.info("ACK: delivery tag {}", tag);
        } catch (Exception ex) {
            logger.error("Error procesando mensaje (delivery tag {}): {}", tag, ex.getMessage(), ex);
            nackToDlq(channel, tag);
        }
    }

    /**
     * Sends the message to the Dead Letter Queue without requeueing.
     *
     * Retries are intentionally not performed at the consumer level:
     * - Deserialization failures are permanent by nature.
     * - Transient transport errors (e.g. SMTP) are retried by JavaMailSender internally.
     * - Infinite-requeue loops are avoided, which was the previous broken behavior.
     *
     * If broker-level retries are needed, configure a retry exchange on the DLQ
     * that re-routes messages back to the main queue after a TTL delay.
     */
    private void nackToDlq(Channel channel, long tag) {
        try {
            channel.basicNack(tag, false, false);
            logger.warn("NACK (DLQ): mensaje enviado a dead-letter queue. Delivery tag: {}", tag);
        } catch (IOException e) {
            logger.error("Error enviando NACK para delivery tag {}: {}", tag, e.getMessage(), e);
        }
    }
}
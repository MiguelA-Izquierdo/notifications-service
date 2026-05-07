package com.app.notificationService.notifications.infrastructure.messaging.inbound;

import com.app.notificationService.notifications.application.events.UserUpdatedEvent;
import com.app.notificationService.notifications.application.events.handlers.UserUpdatedEventHandler;
import com.app.notificationService.notifications.infrastructure.serialization.JsonSerializationService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class UserUpdatedEventListener extends BaseEventListener<UserUpdatedEvent> {

    private final UserUpdatedEventHandler eventHandler;
    private final JsonSerializationService jsonSerializationService;

    public UserUpdatedEventListener(JsonSerializationService jsonSerializationService,
                                    UserUpdatedEventHandler eventHandler,
                                    RabbitTemplate rabbitTemplate,
                                    @Value("${messaging.exchange.retry}") String retryExchange,
                                    ProcessedMessageStore processedMessageStore) {
        super(rabbitTemplate, retryExchange, processedMessageStore);
        this.jsonSerializationService = jsonSerializationService;
        this.eventHandler = eventHandler;
    }

    @RabbitListener(queues = "${messaging.queue.userUpdated}", ackMode = "MANUAL")
    public void onEvent(Message message, Channel channel) {
        handleMessage(message, channel);
    }

    @Override
    protected UserUpdatedEvent deserialize(Message message) {
        String rawJson = new String(message.getBody(), StandardCharsets.UTF_8);
        return new UserUpdatedEvent(
                jsonSerializationService.extractEventId(rawJson),
                jsonSerializationService.deserializePayload(rawJson, UserUpdatedEvent.UserPayload.class)
        );
    }

    @Override
    protected void process(UserUpdatedEvent event) {
        eventHandler.handle(event);
    }
}
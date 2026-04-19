package com.app.notificationService.notifications.infrastructure.messaging.inbound;

import com.app.notificationService.notifications.application.events.UserCreatedEvent;
import com.app.notificationService.notifications.application.events.handlers.UserCreatedEventHandler;
import com.app.notificationService.notifications.infrastructure.serialization.JsonSerializationService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class UserCreatedEventListener extends BaseEventListener<UserCreatedEvent> {

    private final UserCreatedEventHandler eventHandler;
    private final JsonSerializationService jsonSerializationService;

    public UserCreatedEventListener(JsonSerializationService jsonSerializationService,
                                    UserCreatedEventHandler eventHandler) {
        this.jsonSerializationService = jsonSerializationService;
        this.eventHandler = eventHandler;
    }

    @RabbitListener(queues = "${messaging.queue.userCreated}", ackMode = "MANUAL")
    public void onEvent(Message message, Channel channel) {
        handleMessage(message, channel);
    }

    @Override
    protected UserCreatedEvent deserialize(Message message) {
        UserCreatedEvent.UserPayload payload = jsonSerializationService.deserializePayload(
                new String(message.getBody(), StandardCharsets.UTF_8),
                UserCreatedEvent.UserPayload.class
        );
        return new UserCreatedEvent(payload);
    }

    @Override
    protected void process(UserCreatedEvent event) {
        eventHandler.handle(event);
    }
}
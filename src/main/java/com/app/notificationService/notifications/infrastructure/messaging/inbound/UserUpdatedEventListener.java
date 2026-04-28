package com.app.notificationService.notifications.infrastructure.messaging.inbound;

import com.app.notificationService.notifications.application.events.UserUpdatedEvent;
import com.app.notificationService.notifications.application.events.handlers.UserUpdatedEventHandler;
import com.app.notificationService.notifications.infrastructure.serialization.JsonSerializationService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class UserUpdatedEventListener extends BaseEventListener<UserUpdatedEvent> {

    private final UserUpdatedEventHandler eventHandler;
    private final JsonSerializationService jsonSerializationService;

    public UserUpdatedEventListener(JsonSerializationService jsonSerializationService,
                                    UserUpdatedEventHandler eventHandler) {
        this.jsonSerializationService = jsonSerializationService;
        this.eventHandler = eventHandler;
    }

    @RabbitListener(queues = "${messaging.queue.userUpdated}", ackMode = "MANUAL")
    public void onEvent(Message message, Channel channel) {
        handleMessage(message, channel);
    }

    @Override
    protected UserUpdatedEvent deserialize(Message message) {
        UserUpdatedEvent.UserPayload payload = jsonSerializationService.deserializePayload(
                new String(message.getBody(), StandardCharsets.UTF_8),
                UserUpdatedEvent.UserPayload.class
        );
        return new UserUpdatedEvent(payload);
    }

    @Override
    protected void process(UserUpdatedEvent event) {
        eventHandler.handle(event);
    }
}
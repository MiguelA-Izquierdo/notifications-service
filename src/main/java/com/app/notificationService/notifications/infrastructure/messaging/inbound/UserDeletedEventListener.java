package com.app.notificationService.notifications.infrastructure.messaging.inbound;

import com.app.notificationService.notifications.application.events.UserDeletedEvent;
import com.app.notificationService.notifications.application.events.handlers.UserDeletedEventHandler;
import com.app.notificationService.notifications.infrastructure.serialization.JsonSerializationService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class UserDeletedEventListener extends BaseEventListener<UserDeletedEvent> {

    private final UserDeletedEventHandler eventHandler;
    private final JsonSerializationService jsonSerializationService;

    public UserDeletedEventListener(JsonSerializationService jsonSerializationService,
                                    UserDeletedEventHandler eventHandler) {
        this.jsonSerializationService = jsonSerializationService;
        this.eventHandler = eventHandler;
    }

    @RabbitListener(queues = "${messaging.queue.userDeleted}", ackMode = "MANUAL")
    public void onEvent(Message message, Channel channel) {
        handleMessage(message, channel);
    }

    @Override
    protected UserDeletedEvent deserialize(Message message) {
        UserDeletedEvent.UserPayload payload = jsonSerializationService.deserializePayload(
                new String(message.getBody(), StandardCharsets.UTF_8),
                UserDeletedEvent.UserPayload.class
        );
        return new UserDeletedEvent(payload);
    }

    @Override
    protected void process(UserDeletedEvent event) {
        eventHandler.handle(event);
    }
}
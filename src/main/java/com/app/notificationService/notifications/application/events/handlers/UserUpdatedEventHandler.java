package com.app.notificationService.notifications.application.events.handlers;

import com.app.notificationService._shared.domain.bus.event.EventHandler;
import com.app.notificationService.notifications.application.events.UserUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserUpdatedEventHandler implements EventHandler<UserUpdatedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(UserUpdatedEventHandler.class);

    @Override
    public void handle(UserUpdatedEvent event) {
        UserUpdatedEvent.UserPayload payload = event.getPayload();
        payload.changes().forEach((field, ignored) ->
            logger.info("User {} — field '{}' updated", payload.userId(), field)
        );
    }
}
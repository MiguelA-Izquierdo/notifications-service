package com.app.notificationService.notifications.application.events.handlers;

import com.app.notificationService.notifications.application.events.UserUpdatedEvent;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

class UserUpdatedEventHandlerTest {

    private final UserUpdatedEventHandler handler = new UserUpdatedEventHandler();

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void shouldHandleEventWithoutThrowing() {
        UserUpdatedEvent event = new UserUpdatedEvent(new UserUpdatedEvent.UserPayload(
            USER_ID,
            Map.of("name", new UserUpdatedEvent.UserPayload.FieldChange("Miguel", "Mike"))
        ));

        handler.handle(event);
    }

    @Test
    void shouldHandleMultipleChangedFields() {
        UserUpdatedEvent event = new UserUpdatedEvent(new UserUpdatedEvent.UserPayload(
            USER_ID,
            Map.of(
                "name",     new UserUpdatedEvent.UserPayload.FieldChange("Miguel", "Mike"),
                "lastName", new UserUpdatedEvent.UserPayload.FieldChange("Izquierdo", "Smith")
            )
        ));

        handler.handle(event);
    }

    @Test
    void shouldHandleEmptyChanges() {
        UserUpdatedEvent event = new UserUpdatedEvent(new UserUpdatedEvent.UserPayload(
            USER_ID,
            Map.of()
        ));

        handler.handle(event);
    }
}
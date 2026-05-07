package com.app.notificationService.notifications.application.events.handlers;

import com.app.notificationService.notifications.application.events.UserUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;

@ExtendWith(MockitoExtension.class)
class UserUpdatedEventHandlerTest {

    private UserUpdatedEventHandler handler;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @BeforeEach
    void setUp() {
        handler = new UserUpdatedEventHandler();
    }

    private UserUpdatedEvent buildEvent() {
        return new UserUpdatedEvent(
            UUID.fromString("00000000-0000-0000-0000-000000000099"),
            new UserUpdatedEvent.UserPayload(
                USER_ID,
                "carlos@example.com",
                Map.of("name", new UserUpdatedEvent.UserPayload.FieldChange("Carlos", "Charlie"))
            )
        );
    }

    @Test
    void shouldHandleEventWithoutThrowing() {
        assertThatNoException().isThrownBy(() -> handler.handle(buildEvent()));
    }

    @Test
    void shouldHandleMultipleChangedFields() {
        UserUpdatedEvent event = new UserUpdatedEvent(
            UUID.fromString("00000000-0000-0000-0000-000000000099"),
            new UserUpdatedEvent.UserPayload(
                USER_ID,
                "carlos@example.com",
                Map.of(
                    "name",     new UserUpdatedEvent.UserPayload.FieldChange("Carlos", "Charlie"),
                    "lastName", new UserUpdatedEvent.UserPayload.FieldChange("López", "Smith")
                )
            )
        );

        assertThatNoException().isThrownBy(() -> handler.handle(event));
    }

    @Test
    void shouldHandleEmptyChanges() {
        UserUpdatedEvent event = new UserUpdatedEvent(
            UUID.fromString("00000000-0000-0000-0000-000000000099"),
            new UserUpdatedEvent.UserPayload(USER_ID, "carlos@example.com", Map.of())
        );

        assertThatNoException().isThrownBy(() -> handler.handle(event));
    }
}
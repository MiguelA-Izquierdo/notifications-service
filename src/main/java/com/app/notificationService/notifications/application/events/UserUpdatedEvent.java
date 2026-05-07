package com.app.notificationService.notifications.application.events;

import com.app.notificationService._shared.domain.bus.event.Event;

import java.util.Map;
import java.util.UUID;

public class UserUpdatedEvent implements Event<UserUpdatedEvent.UserPayload> {

    private final UUID eventId;
    private final UserPayload payload;

    public UserUpdatedEvent(UUID eventId, UserPayload payload) {
        this.eventId = eventId;
        this.payload = payload;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public UserPayload getPayload() {
        return payload;
    }

    public record UserPayload(UUID userId, String email, Map<String, FieldChange> changes) {
        public record FieldChange(String oldValue, String newValue) {}
    }
}
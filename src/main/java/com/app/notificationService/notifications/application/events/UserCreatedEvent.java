package com.app.notificationService.notifications.application.events;

import com.app.notificationService._shared.domain.bus.event.Event;

import java.util.UUID;

public class UserCreatedEvent implements Event<UserCreatedEvent.UserPayload> {

    private final UUID eventId;
    private final UserPayload payload;

    public UserCreatedEvent(UUID eventId, UserPayload payload) {
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

    public record UserPayload(UUID userId, String name, String lastName, String email) {}
}
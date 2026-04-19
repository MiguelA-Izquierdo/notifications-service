package com.app.notificationService.notifications.application.events;

import com.app.notificationService._shared.domain.bus.event.Event;

import java.util.UUID;

public class UserDeletedEvent implements Event<UserDeletedEvent.UserPayload> {

    private final UserPayload payload;

    public UserDeletedEvent(UserPayload payload) {
        this.payload = payload;
    }

    @Override
    public UserPayload getPayload() {
        return payload;
    }

    public record UserPayload(UUID userId, String name, String lastName, String email) {}
}
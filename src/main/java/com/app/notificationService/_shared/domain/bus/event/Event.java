package com.app.notificationService._shared.domain.bus.event;

import java.util.UUID;

public interface Event<T> {
    UUID getEventId();
    T getPayload();
}
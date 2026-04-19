package com.app.notificationService._shared.domain.bus.event;

public interface Event<T> {
    T getPayload();
}
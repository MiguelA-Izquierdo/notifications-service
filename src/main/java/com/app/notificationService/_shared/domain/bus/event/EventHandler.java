package com.app.notificationService._shared.domain.bus.event;

public interface EventHandler<T> {
    void handle(T event);
}
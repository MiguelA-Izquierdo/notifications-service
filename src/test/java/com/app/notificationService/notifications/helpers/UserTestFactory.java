package com.app.notificationService.notifications.helpers;

import com.app.notificationService.notifications.domain.model.User;

import java.util.UUID;

public class UserTestFactory {

    public static final UUID DEFAULT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    public static User defaultUser() {
        return User.of(DEFAULT_ID, "Miguel", "Izquierdo", "miguel@example.com");
    }

    public static User withName(String name, String lastName, String email) {
        return User.of(DEFAULT_ID, name, lastName, email);
    }
}
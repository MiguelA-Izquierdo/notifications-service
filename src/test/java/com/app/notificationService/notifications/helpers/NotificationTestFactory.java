package com.app.notificationService.notifications.helpers;

import com.app.notificationService.notifications.domain.model.User;
import com.app.notificationService.notifications.domain.model.UserCreatedEmailNotification;
import com.app.notificationService.notifications.domain.model.UserDeletedEmailNotification;
import com.app.notificationService.notifications.domain.valueObject.notification.SubjectEmail;

import java.util.List;

public class NotificationTestFactory {

    public static UserCreatedEmailNotification createdNotification(User user) {
        return UserCreatedEmailNotification.of(
            List.of(user.getEmail()),
            SubjectEmail.of("Test subject"),
            user
        );
    }

    public static UserDeletedEmailNotification deletedNotification(User user) {
        return UserDeletedEmailNotification.of(
            List.of(user.getEmail()),
            SubjectEmail.of("Test subject"),
            user
        );
    }
}
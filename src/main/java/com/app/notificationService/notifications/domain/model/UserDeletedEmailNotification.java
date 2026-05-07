package com.app.notificationService.notifications.domain.model;

import com.app.notificationService.notifications.domain.valueObject.notification.Email;
import com.app.notificationService.notifications.domain.valueObject.notification.SubjectEmail;

import java.util.List;

public class UserDeletedEmailNotification extends EmailNotification<User> {

    private UserDeletedEmailNotification(List<Email> recipientsEmail, SubjectEmail subject, User user) {
        super(recipientsEmail, subject, user);
    }

    public static UserDeletedEmailNotification of(List<Email> recipientsEmail, SubjectEmail subject, User user) {
        return new UserDeletedEmailNotification(recipientsEmail, subject, user);
    }

    @Override
    public String getTemplateName() {
        return "emails/user-deleted";
    }
}
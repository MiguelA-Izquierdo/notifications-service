package com.app.notificationService.notifications.domain.model;

import com.app.notificationService.notifications.domain.valueObject.notification.Email;
import com.app.notificationService.notifications.domain.valueObject.notification.SubjectEmail;

import java.util.List;

public class UserCreatedEmailNotification extends EmailNotification<User> {

    private UserCreatedEmailNotification(List<Email> recipientsEmail, SubjectEmail subject, User user) {
        super(recipientsEmail, subject, user);
    }

    public static UserCreatedEmailNotification of(List<Email> recipientsEmail, User user) {
        SubjectEmail subject = SubjectEmail.of("Bienvenido " + user.getName());
        return new UserCreatedEmailNotification(recipientsEmail, subject, user);
    }

    @Override
    public String getTemplateName() {
        return "emails/user-created";
    }
}
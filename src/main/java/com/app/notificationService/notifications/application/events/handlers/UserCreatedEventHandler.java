package com.app.notificationService.notifications.application.events.handlers;

import com.app.notificationService._shared.domain.bus.event.EventHandler;
import com.app.notificationService.notifications.application.events.UserCreatedEvent;
import com.app.notificationService.notifications.domain.model.User;
import com.app.notificationService.notifications.domain.model.UserCreatedEmailNotification;
import com.app.notificationService.notifications.domain.service.EmailService;
import com.app.notificationService.notifications.domain.valueObject.notification.Email;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCreatedEventHandler implements EventHandler<UserCreatedEvent> {

    private final EmailService emailService;

    public UserCreatedEventHandler(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void handle(UserCreatedEvent event) {
        UserCreatedEvent.UserPayload payload = event.getPayload();
        User user = User.of(payload.userId(), payload.name(), payload.lastName(), payload.email());
        UserCreatedEmailNotification notification = UserCreatedEmailNotification.of(
            List.of(Email.of(payload.email())), user
        );
        emailService.sendEmail(notification);
    }
}
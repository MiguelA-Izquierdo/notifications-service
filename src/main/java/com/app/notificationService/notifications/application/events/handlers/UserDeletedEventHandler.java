package com.app.notificationService.notifications.application.events.handlers;

import com.app.notificationService._shared.domain.bus.event.EventHandler;
import com.app.notificationService.notifications.application.events.UserDeletedEvent;
import com.app.notificationService.notifications.domain.model.User;
import com.app.notificationService.notifications.domain.model.UserDeletedEmailNotification;
import com.app.notificationService.notifications.domain.service.EmailService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDeletedEventHandler implements EventHandler<UserDeletedEvent> {

    private final EmailService emailService;

    public UserDeletedEventHandler(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void handle(UserDeletedEvent event) {
        UserDeletedEvent.UserPayload payload = event.getPayload();
        User user = User.of(payload.userId(), payload.name(), payload.lastName(), payload.email());
        UserDeletedEmailNotification notification = UserDeletedEmailNotification.of(
            List.of(user.getEmail()), user
        );
        emailService.sendEmail(notification);
    }
}
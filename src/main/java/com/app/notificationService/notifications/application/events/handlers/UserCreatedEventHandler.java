package com.app.notificationService.notifications.application.events.handlers;

import com.app.notificationService._shared.domain.bus.event.EventHandler;
import com.app.notificationService.notifications.application.events.UserCreatedEvent;
import com.app.notificationService.notifications.domain.model.User;
import com.app.notificationService.notifications.domain.model.UserCreatedEmailNotification;
import com.app.notificationService.notifications.domain.service.EmailService;
import com.app.notificationService.notifications.domain.valueObject.notification.SubjectEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class UserCreatedEventHandler implements EventHandler<UserCreatedEvent> {

    private final EmailService emailService;
    private final MessageSource messageSource;
    private final Locale defaultLocale;

    public UserCreatedEventHandler(EmailService emailService,
                                   MessageSource messageSource,
                                   @Value("${notification.default-locale:en}") String defaultLocale) {
        this.emailService = emailService;
        this.messageSource = messageSource;
        this.defaultLocale = Locale.forLanguageTag(defaultLocale);
    }

    @Override
    public void handle(UserCreatedEvent event) {
        UserCreatedEvent.UserPayload payload = event.getPayload();
        User user = User.of(payload.userId(), payload.name(), payload.lastName(), payload.email());
        String subjectText = messageSource.getMessage("email.user-created.subject", new Object[]{user.getName()}, defaultLocale);
        UserCreatedEmailNotification notification = UserCreatedEmailNotification.of(
            List.of(user.getEmail()), SubjectEmail.of(subjectText), user
        );
        emailService.sendEmail(notification);
    }
}
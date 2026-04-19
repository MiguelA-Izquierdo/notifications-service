package com.app.notificationService.notifications.domain.service;

import com.app.notificationService.notifications.domain.model.EmailNotification;

public interface EmailService {
    void sendEmail(EmailNotification<?> emailNotification);
}
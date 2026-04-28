package com.app.notificationService.notifications.infrastructure.service;

import com.app.notificationService.notifications.domain.model.EmailNotification;
import com.app.notificationService.notifications.domain.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceLogImplement implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceLogImplement.class);

    @Override
    public void sendEmail(EmailNotification<?> emailNotification) {
        // Email.toString() returns a masked value (u***@domain.com) — safe to log
        logger.info("Correo (simulado) — destinatario: {}, asunto: {}, template: {}",
                emailNotification.getRecipientsEmail(),
                emailNotification.getSubject(),
                emailNotification.getTemplateName());
    }
}
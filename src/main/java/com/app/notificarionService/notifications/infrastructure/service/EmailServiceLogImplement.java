package com.app.notificarionService.notifications.infrastructure.service;

import com.app.notificarionService.notifications.domain.model.EmailNotification;
import com.app.notificarionService.notifications.domain.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Service
public class EmailServiceLogImplement implements EmailService {
  private static final Logger logger = LoggerFactory.getLogger(EmailServiceLogImplement.class);

  public CompletableFuture<Void> sendEmail(EmailNotification emailNotification) {
    return CompletableFuture.runAsync(() -> {
      logger.info("Enviando correo a  {}, con asunto {} con este html {}",
        emailNotification.getRecipientsEmail()
        ,emailNotification.getSubject()
        ,emailNotification.getHtmlBody());
    });

  }
}

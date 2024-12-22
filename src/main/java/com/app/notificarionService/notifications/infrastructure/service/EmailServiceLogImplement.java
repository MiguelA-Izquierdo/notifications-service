package com.app.notificarionService.notifications.infrastructure.service;

import com.app.notificarionService.notifications.domain.service.EmailService;
import com.app.notificarionService.notifications.domain.valueObject.notification.Email;
import com.app.notificarionService.notifications.domain.valueObject.notification.SubjectEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailServiceLogImplement implements EmailService {
  private static final Logger logger = LoggerFactory.getLogger(EmailServiceLogImplement.class);

  public void send(List<Email> recipientsEmail, SubjectEmail subject, String htmlBody) {
    logger.info("Enviando correo a  {}, con asunto {} con este html {}",recipientsEmail,subject,htmlBody);
  }
}

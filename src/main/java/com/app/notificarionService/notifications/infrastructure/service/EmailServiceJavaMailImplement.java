package com.app.notificarionService.notifications.infrastructure.service;

import com.app.notificarionService.notifications.domain.model.EmailNotification;
import com.app.notificarionService.notifications.domain.service.EmailService;
import com.app.notificarionService.notifications.domain.valueObject.notification.Email;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.concurrent.CompletableFuture;

@Service
@Primary
public class EmailServiceJavaMailImplement implements EmailService {
  private static final Logger logger = LoggerFactory.getLogger(EmailServiceJavaMailImplement.class);

  @Autowired
  private JavaMailSender javaMailSender;

  @Autowired
  private TemplateEngine templateEngine;

  @Async
  public CompletableFuture<Void> sendEmail(EmailNotification emailNotification) {
    return CompletableFuture.runAsync(() -> {
      try {
        send(emailNotification);
      } catch (MessagingException e) {
        logger.error("Error al enviar el correo electrónico de forma asíncrona", e);
      }
    });
  }

  private void send(EmailNotification emailNotification) throws MessagingException {
    MimeMessage mimeMessage = javaMailSender.createMimeMessage();

    logger.info("Vamos a enviar un correo a {}", emailNotification.getRecipientsEmail());

    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
    for (Email email : emailNotification.getRecipientsEmail()) {
      helper.addTo(email.getEmail());
    }

    helper.setSubject(emailNotification.getSubject().getValue());

    Context context = new Context();
    context.setVariable("subject", emailNotification.getSubject().getValue());
    context.setVariable("message", emailNotification.getSubject().getValue());
    context.setVariable("htmlContent", emailNotification.getHtmlBody());

    String htmlContent = templateEngine.process("email-template", context);
    helper.setText(htmlContent, true);

    javaMailSender.send(mimeMessage);
    logger.info("Correo enviado a {}", emailNotification.getRecipientsEmail());
  }
}

package com.app.notificationService.notifications.infrastructure.service;

import com.app.notificationService.notifications.domain.model.EmailNotification;
import com.app.notificationService.notifications.domain.service.EmailService;
import com.app.notificationService.notifications.domain.valueObject.notification.Email;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Primary
public class EmailServiceJavaMailImplement implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceJavaMailImplement.class);

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public EmailServiceJavaMailImplement(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendEmail(EmailNotification<?> emailNotification) {
        try {
            send(emailNotification);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo electrónico", e);
        }
    }

    private void send(EmailNotification<?> emailNotification) throws MessagingException {
        // Email.toString() returns a masked value (u***@domain.com) — safe to log
        logger.info("Enviando correo a {}", emailNotification.getRecipientsEmail());

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        for (Email email : emailNotification.getRecipientsEmail()) {
            helper.addTo(email.getEmail());
        }
        helper.setSubject(emailNotification.getSubject().getValue());

        Context context = new Context();
        context.setVariable("subject", emailNotification.getSubject().getValue());
        context.setVariable("htmlContent", emailNotification.getHtmlBody());

        String htmlContent = templateEngine.process("email-template", context);
        helper.setText(htmlContent, true);

        javaMailSender.send(mimeMessage);
        logger.info("Correo enviado a {}", emailNotification.getRecipientsEmail());
    }
}
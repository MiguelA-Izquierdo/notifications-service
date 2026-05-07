package com.app.notificationService.notifications.infrastructure.service;

import com.app.notificationService.notifications.domain.exceptions.EmailSendingException;
import com.app.notificationService.notifications.domain.model.EmailNotification;
import com.app.notificationService.notifications.domain.service.EmailService;
import com.app.notificationService.notifications.domain.valueObject.notification.Email;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

@Service
@Primary
public class EmailServiceJavaMailImplement implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceJavaMailImplement.class);

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final String senderAddress;
    private final Locale defaultLocale;

    public EmailServiceJavaMailImplement(JavaMailSender javaMailSender,
                                         TemplateEngine templateEngine,
                                         @Value("${spring.mail.username}") String senderAddress,
                                         @Value("${notification.default-locale:en}") String defaultLocale) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.senderAddress = senderAddress;
        this.defaultLocale = Locale.forLanguageTag(defaultLocale);
    }

    @Override
    public void sendEmail(EmailNotification<?> emailNotification) {
        String recipients = emailNotification.getRecipientsEmail().toString();
        String template = emailNotification.getTemplateName();
        try {
            send(emailNotification);
        } catch (MessagingException e) {
            throw new EmailSendingException(recipients, template, e);
        }
    }

    private void send(EmailNotification<?> emailNotification) throws MessagingException {
        // Email.toString() returns a masked value (u***@domain.com) — safe to log
        logger.info("Sending email [template={}, recipients={}]", emailNotification.getTemplateName(), emailNotification.getRecipientsEmail());

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setFrom(senderAddress);
        for (Email email : emailNotification.getRecipientsEmail()) {
            helper.addTo(email.getEmail());
        }
        helper.setSubject(emailNotification.getSubject().getValue());

        Context context = new Context(defaultLocale);
        context.setVariable("subject", emailNotification.getSubject().getValue());
        context.setVariable("data", emailNotification.getData());

        String htmlContent = templateEngine.process(emailNotification.getTemplateName(), context);
        helper.setText(htmlContent, true);

        javaMailSender.send(mimeMessage);
        logger.info("Email sent [template={}, recipients={}]", emailNotification.getTemplateName(), emailNotification.getRecipientsEmail());
    }
}
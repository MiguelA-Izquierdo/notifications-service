package com.app.notificarionService.notifications.domain.service;

import com.app.notificarionService.notifications.domain.model.EmailNotification;
import jakarta.mail.MessagingException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EmailService {
  CompletableFuture<Void> sendEmail(EmailNotification emailNotification) throws MessagingException;
}

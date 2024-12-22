package com.app.notificarionService.notifications.domain.service;

import com.app.notificarionService.notifications.domain.valueObject.notification.Email;
import com.app.notificarionService.notifications.domain.valueObject.notification.SubjectEmail;

import java.util.List;

public interface EmailService {
  void send(List<Email> recipientsEmail, SubjectEmail subject, String htmlBody);
}

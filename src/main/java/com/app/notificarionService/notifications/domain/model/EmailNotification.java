package com.app.notificarionService.notifications.domain.model;

import com.app.notificarionService.notifications.domain.valueObject.notification.Email;
import com.app.notificarionService.notifications.domain.valueObject.notification.SubjectEmail;

import java.util.List;

public abstract  class EmailNotification {
  private final List<Email> recipientsEmail;
  private final SubjectEmail subject;
  private String htmlBody;
  public EmailNotification(List<Email> recipientsEmail, SubjectEmail subject){
    this.recipientsEmail = recipientsEmail;
    this.subject = subject;
    this.htmlBody = "";
  }

  public List<Email> getRecipientsEmail() {
    return recipientsEmail;
  }

  public SubjectEmail getSubject() {
    return subject;
  }

  public String getHtmlBody() {
    return htmlBody;
  }

  public void setHtmlBody(String htmlBody) {
    this.htmlBody = htmlBody;
  }

  public abstract <T> String generateHtml(T... data);
}

package com.app.notificationService.notifications.domain.model;

import com.app.notificationService.notifications.domain.valueObject.notification.Email;
import com.app.notificationService.notifications.domain.valueObject.notification.SubjectEmail;

import java.util.List;

public abstract class EmailNotification<D> {

    private final List<Email> recipientsEmail;
    private final SubjectEmail subject;
    private final String htmlBody;

    protected EmailNotification(List<Email> recipientsEmail, SubjectEmail subject, D data) {
        this.recipientsEmail = recipientsEmail;
        this.subject = subject;
        this.htmlBody = generateHtml(data);
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

    protected abstract String generateHtml(D data);
}
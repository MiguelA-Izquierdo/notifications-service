package com.app.notificationService.notifications.domain.exceptions;

public class EmailSendingException extends RuntimeException {

    public EmailSendingException(String recipients, String template, Throwable cause) {
        super(String.format("Failed to send email [template=%s, recipients=%s]", template, recipients), cause);
    }
}
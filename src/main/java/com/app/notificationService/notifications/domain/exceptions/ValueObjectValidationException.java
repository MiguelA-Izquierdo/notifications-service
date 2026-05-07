package com.app.notificationService.notifications.domain.exceptions;


public class ValueObjectValidationException extends RuntimeException {

    public ValueObjectValidationException(String field, String message) {
        super(message);
    }
}

package com.app.notificationService.notifications.domain.valueObject.notification;

import com.app.notificationService.notifications.domain.exceptions.ValueObjectValidationException;

final public class SubjectEmail {

    private final String value;

    private SubjectEmail(String value) {
        this.value = validate(value);
    }

    public static SubjectEmail of(String value) {
        return new SubjectEmail(value);
    }

    public String getValue() {
        return value;
    }

    private String validate(String value) {
        if (value == null) {
            throw new ValueObjectValidationException("SubjectEmail", "SubjectEmail cannot be null");
        }
        String sanitized = value.replaceAll("[\\r\\n\\x00]", "");
        if (sanitized.trim().isEmpty()) {
            throw new ValueObjectValidationException("SubjectEmail", "SubjectEmail cannot be blank");
        }
        if (sanitized.length() < 5 || sanitized.length() > 60) {
            throw new ValueObjectValidationException("SubjectEmail", "SubjectEmail must be between 5 and 60 characters");
        }
        return sanitized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectEmail that = (SubjectEmail) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}

package com.app.notificarionService.notifications.domain.valueObject.notification;


import com.app.notificarionService.notifications.domain.exceptions.ValueObjectValidationException;

import java.util.regex.Pattern;


public class Email {
  private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
  private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

  private final String email;
  private Email(String email) {
    if (!isValid(email)) {
      throw new ValueObjectValidationException("Email","Invalid email address: " + email);
    }
    this.email = email;
  }

  public static Email of(String email) {
    return new Email(email);
  }

  private static boolean isValid(String email) {
    return email != null && EMAIL_PATTERN.matcher(email).matches();
  }

  public String getEmail() {
    return email;
  }

  @Override
  public String toString() {
    return email;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Email that = (Email) obj;
    return email.equals(that.email);
  }

  @Override
  public int hashCode() {
    return email.hashCode();
  }
}

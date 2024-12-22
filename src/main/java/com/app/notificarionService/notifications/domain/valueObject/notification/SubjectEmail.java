package com.app.notificarionService.notifications.domain.valueObject.notification;

import java.util.Objects;

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
    Objects.requireNonNull(value, "SubjectEmail description cannot be null");
    if (value.trim().isEmpty()) {
      throw new IllegalArgumentException("SubjectEmail name cannot be empty");
    }
    if (value.length() < 5 || value.length() > 60) {
      throw new IllegalArgumentException("The SubjectEmail must be between 5 and 60 characters long.");
    }

    return value;
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
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value;
  }
}

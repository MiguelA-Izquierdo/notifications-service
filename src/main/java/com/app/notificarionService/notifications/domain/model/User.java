package com.app.notificarionService.notifications.domain.model;

import com.app.notificarionService.notifications.domain.valueObject.notification.Email;

import java.util.UUID;

public class User {
  private final UUID id;
  private final String name;
  private final String lastName;
  private final Email email;

  private User(UUID id, String name, String lastName, Email email) {
    this.id = id;
    this.name = name;
    this.lastName = lastName;
    this.email = email;
  }

  public static User of(UUID userId, String userName, String userLastName, String userEmail) {
    return new User(
      userId,
      userName,
      userLastName,
      Email.of(userEmail)
    );
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getLastName() {
    return lastName;
  }

  public Email getEmail() {
    return email;
  }
}

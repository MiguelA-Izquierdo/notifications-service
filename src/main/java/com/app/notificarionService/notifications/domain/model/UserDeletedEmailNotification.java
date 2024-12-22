package com.app.notificarionService.notifications.domain.model;

import com.app.notificarionService.notifications.domain.valueObject.notification.Email;
import com.app.notificarionService.notifications.domain.valueObject.notification.SubjectEmail;

import java.util.List;

public class UserDeletedEmailNotification extends EmailNotification {

  private UserDeletedEmailNotification(List<Email> recipientsEmail,
                                       SubjectEmail subject,
                                       User user) {
    super(recipientsEmail, subject);
    setHtmlBody(generateHtml(user));
  }

  public static UserDeletedEmailNotification of(List<Email> recipientsEmail, User user) {
    SubjectEmail subject = SubjectEmail.of("Hasta pronto " + user.getName());
    return new UserDeletedEmailNotification(recipientsEmail, subject, user);
  }

  @SafeVarargs
  @Override
  public final <T> String generateHtml(T... data) {
    if (data.length == 1 && data[0] instanceof User user) {
      String userName = user.getName();

      return "<h1>¡Adiós, " + userName + "!</h1>\n" +
        "<p>Sentimos mucho que hayas decidido dejar nuestra comunidad. Siempre valoramos tu presencia y el tiempo que compartiste con nosotros.</p>\n" +
        "<p>Esperamos que hayas disfrutado de las novedades, beneficios y herramientas que te ofrecimos.</p>\n" +
        "<p>Si alguna vez decides volver, estaremos encantados de darte la bienvenida nuevamente. Mientras tanto, te deseamos lo mejor en tus próximos pasos.</p>\n" +
        "<p>No dudes en contactarnos si necesitas algo. ¡Siempre estaremos aquí para ti!</p>\n";
    } else {
      throw new IllegalArgumentException("Invalid data types. Expected User.");
    }
  }
}

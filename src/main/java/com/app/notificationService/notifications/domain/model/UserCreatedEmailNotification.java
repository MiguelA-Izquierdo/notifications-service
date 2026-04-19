package com.app.notificationService.notifications.domain.model;

import com.app.notificationService.notifications.domain.valueObject.notification.Email;
import com.app.notificationService.notifications.domain.valueObject.notification.SubjectEmail;

import java.util.List;

public class UserCreatedEmailNotification extends EmailNotification<User> {

    private UserCreatedEmailNotification(List<Email> recipientsEmail, SubjectEmail subject, User user) {
        super(recipientsEmail, subject, user);
    }

    public static UserCreatedEmailNotification of(List<Email> recipientsEmail, User user) {
        SubjectEmail subject = SubjectEmail.of("Bienvenido " + user.getName());
        return new UserCreatedEmailNotification(recipientsEmail, subject, user);
    }

    @Override
    protected String generateHtml(User user) {
        return "<h1>¡Hola, " + user.getName() + "!</h1>\n" +
                "<p>¡Bienvenido/a a nuestra comunidad! Nos alegra mucho que te hayas unido a nosotros.</p>\n" +
                "<p>A partir de ahora, estarás al tanto de todas las novedades, beneficios y herramientas que tenemos para ofrecerte.</p>\n" +
                "<p>Si tienes alguna duda o necesitas ayuda, no dudes en contactarnos. ¡Estamos aquí para ti!</p>\n";
    }
}
package com.app.notificationService.notifications.domain.model;

import com.app.notificationService.notifications.domain.valueObject.notification.Email;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNotificationTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final List<Email> RECIPIENTS = List.of(Email.of("admin@example.com"));

    private User buildUser() {
        return User.of(ID, "Miguel", "Izquierdo", "miguel@example.com");
    }

    // --- UserCreatedEmailNotification ---

    @Test
    void createdNotification_shouldContainRecipients() {
        UserCreatedEmailNotification n = UserCreatedEmailNotification.of(RECIPIENTS, buildUser());
        assertThat(n.getRecipientsEmail()).isEqualTo(RECIPIENTS);
    }

    @Test
    void createdNotification_subjectShouldIncludeName() {
        UserCreatedEmailNotification n = UserCreatedEmailNotification.of(RECIPIENTS, buildUser());
        assertThat(n.getSubject().getValue()).contains("Miguel");
    }

    @Test
    void createdNotification_shouldReturnCorrectTemplateName() {
        UserCreatedEmailNotification n = UserCreatedEmailNotification.of(RECIPIENTS, buildUser());
        assertThat(n.getTemplateName()).isEqualTo("emails/user-created");
    }

    @Test
    void createdNotification_dataShouldBeTheUser() {
        User user = buildUser();
        UserCreatedEmailNotification n = UserCreatedEmailNotification.of(RECIPIENTS, user);
        assertThat(n.getData()).isSameAs(user);
    }

    // --- UserDeletedEmailNotification ---

    @Test
    void deletedNotification_shouldContainRecipients() {
        UserDeletedEmailNotification n = UserDeletedEmailNotification.of(RECIPIENTS, buildUser());
        assertThat(n.getRecipientsEmail()).isEqualTo(RECIPIENTS);
    }

    @Test
    void deletedNotification_subjectShouldIncludeName() {
        UserDeletedEmailNotification n = UserDeletedEmailNotification.of(RECIPIENTS, buildUser());
        assertThat(n.getSubject().getValue()).contains("Miguel");
    }

    @Test
    void deletedNotification_shouldReturnCorrectTemplateName() {
        UserDeletedEmailNotification n = UserDeletedEmailNotification.of(RECIPIENTS, buildUser());
        assertThat(n.getTemplateName()).isEqualTo("emails/user-deleted");
    }

    @Test
    void deletedNotification_dataShouldBeTheUser() {
        User user = buildUser();
        UserDeletedEmailNotification n = UserDeletedEmailNotification.of(RECIPIENTS, user);
        assertThat(n.getData()).isSameAs(user);
    }
}
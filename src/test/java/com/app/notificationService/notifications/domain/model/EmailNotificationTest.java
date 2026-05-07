package com.app.notificationService.notifications.domain.model;

import com.app.notificationService.notifications.helpers.NotificationTestFactory;
import com.app.notificationService.notifications.helpers.UserTestFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNotificationTest {

    // --- UserCreatedEmailNotification ---

    @Test
    void createdNotification_shouldContainRecipients() {
        User user = UserTestFactory.defaultUser();
        UserCreatedEmailNotification n = NotificationTestFactory.createdNotification(user);

        assertThat(n.getRecipientsEmail()).containsExactly(user.getEmail());
    }

    @Test
    void createdNotification_subjectShouldBeStored() {
        UserCreatedEmailNotification n = NotificationTestFactory.createdNotification(UserTestFactory.defaultUser());

        assertThat(n.getSubject().getValue()).isEqualTo("Test subject");
    }

    @Test
    void createdNotification_shouldReturnCorrectTemplateName() {
        UserCreatedEmailNotification n = NotificationTestFactory.createdNotification(UserTestFactory.defaultUser());

        assertThat(n.getTemplateName()).isEqualTo("emails/user-created");
    }

    @Test
    void createdNotification_dataShouldBeTheUser() {
        User user = UserTestFactory.defaultUser();
        UserCreatedEmailNotification n = NotificationTestFactory.createdNotification(user);

        assertThat(n.getData()).isSameAs(user);
    }

    // --- UserDeletedEmailNotification ---

    @Test
    void deletedNotification_shouldContainRecipients() {
        User user = UserTestFactory.defaultUser();
        UserDeletedEmailNotification n = NotificationTestFactory.deletedNotification(user);

        assertThat(n.getRecipientsEmail()).containsExactly(user.getEmail());
    }

    @Test
    void deletedNotification_subjectShouldBeStored() {
        UserDeletedEmailNotification n = NotificationTestFactory.deletedNotification(UserTestFactory.defaultUser());

        assertThat(n.getSubject().getValue()).isEqualTo("Test subject");
    }

    @Test
    void deletedNotification_shouldReturnCorrectTemplateName() {
        UserDeletedEmailNotification n = NotificationTestFactory.deletedNotification(UserTestFactory.defaultUser());

        assertThat(n.getTemplateName()).isEqualTo("emails/user-deleted");
    }

    @Test
    void deletedNotification_dataShouldBeTheUser() {
        User user = UserTestFactory.defaultUser();
        UserDeletedEmailNotification n = NotificationTestFactory.deletedNotification(user);

        assertThat(n.getData()).isSameAs(user);
    }
}
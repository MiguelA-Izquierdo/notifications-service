package com.app.notificationService.notifications.domain.valueObject;

import com.app.notificationService.notifications.domain.exceptions.ValueObjectValidationException;
import com.app.notificationService.notifications.domain.valueObject.notification.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "user.name+tag@sub.domain.org",
            "a@b.io"
    })
    void shouldAcceptValidEmails(String address) {
        Email email = Email.of(address);
        assertThat(email.getEmail()).isEqualTo(address);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-an-email",
            "@domain.com",
            "user@",
            "user @domain.com",
            "",
    })
    void shouldRejectInvalidEmails(String address) {
        assertThatThrownBy(() -> Email.of(address))
                .isInstanceOf(ValueObjectValidationException.class);
    }

    @Test
    void shouldThrowForNullEmail() {
        assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(ValueObjectValidationException.class);
    }

    @Test
    void shouldMaskEmailInToString() {
        Email email = Email.of("john@example.com");
        String masked = email.toString();

        assertThat(masked).startsWith("j***");
        assertThat(masked).endsWith("@example.com");
        assertThat(masked).doesNotContain("john");
    }

    @Test
    void shouldPreserveRawEmailViaGetter() {
        Email email = Email.of("john@example.com");
        assertThat(email.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldImplementEquality() {
        Email a = Email.of("user@example.com");
        Email b = Email.of("user@example.com");
        Email c = Email.of("other@example.com");

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
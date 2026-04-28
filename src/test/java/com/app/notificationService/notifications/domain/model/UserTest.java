package com.app.notificationService.notifications.domain.model;

import com.app.notificationService.notifications.domain.exceptions.ValueObjectValidationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void shouldCreateUserWithValidData() {
        User user = User.of(ID, "Miguel", "Izquierdo", "miguel@example.com");

        assertThat(user.getId()).isEqualTo(ID);
        assertThat(user.getName()).isEqualTo("Miguel");
        assertThat(user.getLastName()).isEqualTo("Izquierdo");
        assertThat(user.getEmail().getEmail()).isEqualTo("miguel@example.com");
    }

    @Test
    void shouldThrowWhenEmailIsInvalid() {
        assertThatThrownBy(() -> User.of(ID, "Miguel", "Izquierdo", "not-an-email"))
                .isInstanceOf(ValueObjectValidationException.class);
    }

    @Test
    void shouldThrowWhenEmailIsNull() {
        assertThatThrownBy(() -> User.of(ID, "Miguel", "Izquierdo", null))
                .isInstanceOf(ValueObjectValidationException.class);
    }
}
package com.app.notificationService.notifications.domain.valueObject;

import com.app.notificationService.notifications.domain.exceptions.ValueObjectValidationException;
import com.app.notificationService.notifications.domain.valueObject.notification.SubjectEmail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubjectEmailTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "Hola!",
            "Bienvenido a nuestra plataforma",
            "12345"
    })
    void shouldAcceptValidSubjects(String value) {
        SubjectEmail subject = SubjectEmail.of(value);
        assertThat(subject.getValue()).isEqualTo(value);
    }

    @Test
    void shouldThrowForNullSubject() {
        assertThatThrownBy(() -> SubjectEmail.of(null))
                .isInstanceOf(ValueObjectValidationException.class)
                .hasMessageContaining("null");
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", ""})
    void shouldThrowForBlankSubject(String value) {
        assertThatThrownBy(() -> SubjectEmail.of(value))
                .isInstanceOf(ValueObjectValidationException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void shouldThrowWhenTooShort() {
        assertThatThrownBy(() -> SubjectEmail.of("Hi"))
                .isInstanceOf(ValueObjectValidationException.class)
                .hasMessageContaining("between 5 and 60");
    }

    @Test
    void shouldThrowWhenTooLong() {
        String longSubject = "A".repeat(61);
        assertThatThrownBy(() -> SubjectEmail.of(longSubject))
                .isInstanceOf(ValueObjectValidationException.class)
                .hasMessageContaining("between 5 and 60");
    }

    @Test
    void shouldAcceptExactBoundaries() {
        SubjectEmail min = SubjectEmail.of("12345");
        SubjectEmail max = SubjectEmail.of("A".repeat(60));
        assertThat(min.getValue()).hasSize(5);
        assertThat(max.getValue()).hasSize(60);
    }

    @Test
    void shouldImplementEquality() {
        SubjectEmail a = SubjectEmail.of("Asunto válido");
        SubjectEmail b = SubjectEmail.of("Asunto válido");
        SubjectEmail c = SubjectEmail.of("Otro asunto");

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void shouldReturnValueInToString() {
        SubjectEmail subject = SubjectEmail.of("Asunto válido");
        assertThat(subject.toString()).isEqualTo("Asunto válido");
    }
}
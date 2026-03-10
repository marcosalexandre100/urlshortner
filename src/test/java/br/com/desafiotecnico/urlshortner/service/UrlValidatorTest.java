package br.com.desafiotecnico.urlshortner.service;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlValidatorTest {

    @Test
    void shouldAcceptHttpUrl() {
        assertDoesNotThrow(() ->
                UrlValidator.validateHttpUrl("http://google.com")
        );
    }

    @Test
    void shouldThrowExceptionWhenUrlIsNull() {
        assertThrows(
                ValidationException.class,
                () -> UrlValidator.validateHttpUrl(null)
        );
    }

    @Test
    void shouldThrowExceptionWhenUrlIsBlank() {
        assertThrows(
                ValidationException.class,
                () -> UrlValidator.validateHttpUrl("   ")
        );
    }

    @Test
    void shouldThrowExceptionWhenUrlIsInvalid() {
        assertThrows(
                ValidationException.class,
                () -> UrlValidator.validateHttpUrl("ht@tp://invalid")
        );
    }
}
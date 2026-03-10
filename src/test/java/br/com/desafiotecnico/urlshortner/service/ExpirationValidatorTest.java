package br.com.desafiotecnico.urlshortner.service;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ExpirationValidatorTest {


    @Test
    void shouldThrowException(){
        Instant expirationDate = Instant.parse("2026-03-01T10:00:00Z");
        assertThrows(ValidationException.class, ()-> ExpirationValidator.validateExpiration(expirationDate) );
    }

    @Test
    void notshouldThrowException(){
        Instant expirationDate = Instant.parse("2036-03-01T10:00:00Z");
        assertDoesNotThrow(()-> ExpirationValidator.validateExpiration(expirationDate) );
    }

}
package br.com.desafiotecnico.urlshortner.service;

import br.com.desafiotecnico.urlshortner.exception.UrlExpiredException;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ExpirationValidator {

    private ExpirationValidator() {}


    public static void validateExpiration(Instant expirationDate) {
        if (expirationDate == null) {
            return;
        }

        if (isExpired(expirationDate)) {
            throw new ValidationException("Expiration date must be in the future");
        }
    }

    public static void ensureNotExpired(Instant expirationDate) {
        if (isExpired(expirationDate)) {
            throw new UrlExpiredException("URL expired");
        }
    }

    private static boolean isExpired(Instant expirationDate) {
        return expirationDate != null && expirationDate.isBefore(Instant.now());
    }
}

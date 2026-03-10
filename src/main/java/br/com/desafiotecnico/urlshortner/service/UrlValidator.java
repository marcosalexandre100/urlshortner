package br.com.desafiotecnico.urlshortner.service;

import jakarta.validation.ValidationException;

import java.net.URI;


public final class UrlValidator {

    private UrlValidator() {}

    public static void validateHttpUrl(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("originalUrl is required");
        }

        final URI uri;
        try {
            uri = URI.create(value.trim());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("originalUrl must be a valid URL");
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new ValidationException("originalUrl must start with http or https");
        }
    }
}
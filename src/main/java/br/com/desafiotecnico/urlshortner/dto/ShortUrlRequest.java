package br.com.desafiotecnico.urlshortner.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record ShortUrlRequest(
        @NotBlank String originalUrl,
        Instant expirationDate
) {
}

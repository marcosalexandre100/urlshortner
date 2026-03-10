package br.com.desafiotecnico.urlshortner.dto;

import java.time.Instant;

public record ShortUrlResponse(
        String id,
        String shortUrl,
        String originalUrl,
        Instant createdAt,
        Instant expirationDate,
        Long clickCount
) {}
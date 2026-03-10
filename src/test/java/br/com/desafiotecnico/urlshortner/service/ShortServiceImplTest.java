package br.com.desafiotecnico.urlshortner.service;


import br.com.desafiotecnico.urlshortner.config.AppProperties;
import br.com.desafiotecnico.urlshortner.domain.ShortUrl;
import br.com.desafiotecnico.urlshortner.dto.ShortUrlRequest;
import br.com.desafiotecnico.urlshortner.dto.ShortUrlResponse;
import br.com.desafiotecnico.urlshortner.exception.UrlExpiredException;
import br.com.desafiotecnico.urlshortner.exception.UrlNotFoundException;
import br.com.desafiotecnico.urlshortner.repository.ShortUrlRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortServiceImplTest {

    @Mock
    private ShortUrlRepository repository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private ShortServiceImpl service;

    @Test
    void shouldCreateShortUrlSuccessfully() {
        ShortUrlRequest request = new ShortUrlRequest(
                "https://www.google.com",
                Instant.parse("2027-12-31T23:59:59Z")
        );

        when(shortCodeGenerator.generate(7)).thenReturn("abc1234");
        when(repository.existsByShortCode("abc1234")).thenReturn(false);
        when(appProperties.baseUrl()).thenReturn("http://localhost:8080");

        when(repository.save(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShortUrlResponse response = service.createShortUrl(request);

        assertNotNull(response);
        assertEquals("abc1234", response.id());
        assertEquals("http://localhost:8080/abc1234", response.shortUrl());
        assertEquals("https://www.google.com", response.originalUrl());
        assertEquals(0L, response.clickCount());

        verify(repository).save(any(ShortUrl.class));
    }

    @Test
    void shouldRetryWhenGeneratedCodeAlreadyExists() {
        ShortUrlRequest request = new ShortUrlRequest(
                "https://www.google.com",
                null
        );

        when(shortCodeGenerator.generate(7)).thenReturn("dup1234", "unique77");
        when(repository.existsByShortCode("dup1234")).thenReturn(true);
        when(repository.existsByShortCode("unique77")).thenReturn(false);
        when(appProperties.baseUrl()).thenReturn("http://localhost:8080");
        when(repository.save(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShortUrlResponse response = service.createShortUrl(request);

        assertEquals("unique77", response.id());
        assertEquals("http://localhost:8080/unique77", response.shortUrl());

        verify(shortCodeGenerator, times(2)).generate(7);
    }

    @Test
    void shouldThrowValidationExceptionWhenCouldNotGenerateUniqueCode() {
        ShortUrlRequest request = new ShortUrlRequest(
                "https://www.google.com",
                null
        );

        when(shortCodeGenerator.generate(7)).thenReturn(
                "code001", "code002", "code003", "code004", "code005",
                "code006", "code007", "code008", "code009", "code010"
        );
        when(repository.existsByShortCode(anyString())).thenReturn(true);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.createShortUrl(request)
        );

        assertEquals("Could not generate unique short code", ex.getMessage());
        verify(shortCodeGenerator, times(10)).generate(7);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRedirectOriginalUrlAndIncrementClickCount() {
        ShortUrl shortUrl = ShortUrl.builder()
                .id(1L)
                .shortCode("abc1234")
                .originalUrl("https://www.google.com")
                .createdAt(Instant.now())
                .expirationDate(Instant.now().plusSeconds(3600))
                .clickCount(0L)
                .build();

        when(repository.findByShortCode("abc1234")).thenReturn(Optional.of(shortUrl));

        String originalUrl = service.redirectOriginalUrl("abc1234");

        assertEquals("https://www.google.com", originalUrl);
        assertEquals(1L, shortUrl.getClickCount());
    }

    @Test
    void shouldThrowNotFoundWhenRedirectCodeDoesNotExist() {
        when(repository.findByShortCode("notfound")).thenReturn(Optional.empty());

        UrlNotFoundException ex = assertThrows(
                UrlNotFoundException.class,
                () -> service.redirectOriginalUrl("notfound")
        );

        assertEquals("URL not found", ex.getMessage());
    }

    @Test
    void shouldThrowExpiredWhenRedirectingExpiredUrl() {
        ShortUrl shortUrl = ShortUrl.builder()
                .id(1L)
                .shortCode("expired1")
                .originalUrl("https://www.google.com")
                .createdAt(Instant.now())
                .expirationDate(Instant.now().minusSeconds(60))
                .clickCount(0L)
                .build();

        when(repository.findByShortCode("expired1")).thenReturn(Optional.of(shortUrl));

        UrlExpiredException ex = assertThrows(
                UrlExpiredException.class,
                () -> service.redirectOriginalUrl("expired1")
        );

        assertEquals("URL expired", ex.getMessage());
    }

    @Test
    void shouldReturnUrlDetails() {
        ShortUrl shortUrl = ShortUrl.builder()
                .id(1L)
                .shortCode("abc1234")
                .originalUrl("https://www.google.com")
                .createdAt(Instant.parse("2026-03-08T10:00:00Z"))
                .expirationDate(Instant.parse("2027-12-31T23:59:59Z"))
                .clickCount(5L)
                .build();

        when(repository.findByShortCode("abc1234")).thenReturn(Optional.of(shortUrl));
        when(appProperties.baseUrl()).thenReturn("http://localhost:8080");

        ShortUrlResponse response = service.getUrlDetails("abc1234");

        assertEquals("abc1234", response.id());
        assertEquals("http://localhost:8080/abc1234", response.shortUrl());
        assertEquals("https://www.google.com", response.originalUrl());
        assertEquals(5L, response.clickCount());
    }

    @Test
    void shouldThrowNotFoundWhenGettingDetailsOfNonExistentCode() {
        when(repository.findByShortCode("missing")).thenReturn(Optional.empty());

        UrlNotFoundException ex = assertThrows(
                UrlNotFoundException.class,
                () -> service.getUrlDetails("missing")
        );

        assertEquals("URL not found", ex.getMessage());
    }

    @Test
    void shouldListAllUrls() {
        ShortUrl shortUrl1 = ShortUrl.builder()
                .id(1L)
                .shortCode("abc1234")
                .originalUrl("https://www.google.com")
                .createdAt(Instant.parse("2026-03-08T10:00:00Z"))
                .expirationDate(null)
                .clickCount(1L)
                .build();

        ShortUrl shortUrl2 = ShortUrl.builder()
                .id(2L)
                .shortCode("xyz5678")
                .originalUrl("https://www.github.com")
                .createdAt(Instant.parse("2026-03-08T11:00:00Z"))
                .expirationDate(null)
                .clickCount(2L)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);
        Page<ShortUrl> page = new PageImpl<>(List.of(shortUrl1, shortUrl2), pageable, 2);

        when(repository.findAll(pageable)).thenReturn(page);
        when(appProperties.baseUrl()).thenReturn("http://localhost:8080");

        Page<ShortUrlResponse> responsePage = service.listAll(pageable);

        assertEquals(2, responsePage.getTotalElements());
        assertEquals("abc1234", responsePage.getContent().get(0).id());
        assertEquals("xyz5678", responsePage.getContent().get(1).id());
        assertEquals("http://localhost:8080/abc1234", responsePage.getContent().get(0).shortUrl());
    }
}
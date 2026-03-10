package br.com.desafiotecnico.urlshortner.service;

import br.com.desafiotecnico.urlshortner.dto.ShortUrlRequest;
import br.com.desafiotecnico.urlshortner.dto.ShortUrlResponse;
import br.com.desafiotecnico.urlshortner.config.AppProperties;
import br.com.desafiotecnico.urlshortner.domain.ShortUrl;
import br.com.desafiotecnico.urlshortner.exception.UrlExpiredException;
import br.com.desafiotecnico.urlshortner.repository.ShortUrlRepository;
import br.com.desafiotecnico.urlshortner.exception.UrlNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortServiceImpl implements ShortService {

    private final ShortUrlRepository repository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public ShortUrlResponse createShortUrl(ShortUrlRequest shortUrlRequest) {

         UrlValidator.validateHttpUrl(shortUrlRequest.originalUrl());
         ExpirationValidator.validateExpiration(shortUrlRequest.expirationDate());

        String code = generateUniqueCode();

        ShortUrl entity = ShortUrl.builder()
                .shortCode(code)
                .originalUrl(shortUrlRequest.originalUrl())
                .createdAt(Instant.now())
                .expirationDate(shortUrlRequest.expirationDate())
                .clickCount(0L)
                .build();
        ShortUrl saved = repository.save(entity);

        log.info("New short URL created. code={}, originalUrl={}",
                code, shortUrlRequest.originalUrl());

        return toResponse(saved);
    }

    private String generateUniqueCode(){
        int attempts = 0;
        while (attempts < 10){
            String code = shortCodeGenerator.generate(7);
            if(!repository.existsByShortCode(code)){
                return code;
            }
            attempts++;
        }
        throw new ValidationException("Could not generate unique short code");
    }

    private ShortUrlResponse toResponse(ShortUrl entity){
        String shortUrl = appProperties.baseUrl() + "/" + entity.getShortCode();
        return new ShortUrlResponse(
                entity.getShortCode(),
                shortUrl,
                entity.getOriginalUrl(),
                entity.getCreatedAt(),
                entity.getExpirationDate(),
                entity.getClickCount()
        );
    }

    @Override
    @Transactional
    public String redirectOriginalUrl(String shortCode) {
        ShortUrl shortUrl = findByShortCode(shortCode);

        try {
            ExpirationValidator.ensureNotExpired(shortUrl.getExpirationDate());
        } catch (UrlExpiredException ex) {
            log.warn("Attempt to access expired short URL. code={}, expirationDate={}",
                    shortCode, shortUrl.getExpirationDate());
            throw ex;
        }

        shortUrl.setClickCount(shortUrl.getClickCount() + 1);

        log.info("Short URL redirected successfully. code={}, clickCount={}",
                shortCode, shortUrl.getClickCount());

        return shortUrl.getOriginalUrl();
    }

    @Override
    public ShortUrlResponse getUrlDetails(String shortCode) {

        ShortUrl shortUrl = findByShortCode(shortCode);
        return toResponse(shortUrl);
    }

    @Override
    public Page<ShortUrlResponse> listAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    private ShortUrl findByShortCode(String shortCode){
        return repository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("Attempt to access non-existent short URL. code={}", shortCode);
                    return new UrlNotFoundException("URL not found");
                });
    }
}

package br.com.desafiotecnico.urlshortner.controller;

import br.com.desafiotecnico.urlshortner.config.AppProperties;
import br.com.desafiotecnico.urlshortner.dto.ShortUrlRequest;
import br.com.desafiotecnico.urlshortner.dto.ShortUrlResponse;
import br.com.desafiotecnico.urlshortner.exception.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import br.com.desafiotecnico.urlshortner.service.ShortService;
import java.net.URI;
import java.util.Objects;

@RestController
@RequestMapping("/v1/urls")
@RequiredArgsConstructor
@Slf4j
public class ShortUrlController {

    private final ShortService service;
    private final AppProperties appProperties;

    @PostMapping
    public ResponseEntity<ShortUrlResponse> create(@RequestHeader(value = "X-API-Key", required = false) String apiKey,
                                                   @RequestBody @Valid ShortUrlRequest urlRequest,
                                                     UriComponentsBuilder uriBuilder){

        if(!(Objects.equals(apiKey, appProperties.apiKey()))){
            log.warn("Unauthorized request to create short URL. originalUrl={}", urlRequest.originalUrl());
            throw new UnauthorizedException("X-API-Key is invalid or missing");
        }

        log.info("Received request to create short URL. originalUrl={}", urlRequest.originalUrl());

        var response = service.createShortUrl(urlRequest);
        URI location = uriBuilder.path("/v1/urls/{id}").buildAndExpand(response.id()).toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/redirect/{id}")
    public ResponseEntity<Void> redirect(@PathVariable String id){

        log.info("Received request to redirect short URL. code={}", id);

        String originalUrl = service.redirectOriginalUrl(id);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();

    }

    @GetMapping("/{id}")
    public ResponseEntity<ShortUrlResponse> getDetails(@PathVariable String id){
        log.info("Received request to get short URL details. code={}", id);
        var response = service.getUrlDetails(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ShortUrlResponse>> listAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable
            ){
        log.info("Received request to list short URLs. page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<ShortUrlResponse> response = service.listAll(pageable);
        return ResponseEntity.ok(response);
    }
}

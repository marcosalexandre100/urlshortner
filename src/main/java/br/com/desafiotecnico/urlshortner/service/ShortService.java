package br.com.desafiotecnico.urlshortner.service;

import br.com.desafiotecnico.urlshortner.dto.ShortUrlRequest;
import br.com.desafiotecnico.urlshortner.dto.ShortUrlResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShortService {

    ShortUrlResponse createShortUrl(ShortUrlRequest shortUrlRequest);

    String redirectOriginalUrl(String shortCode);

    ShortUrlResponse getUrlDetails(String id);

    Page<ShortUrlResponse> listAll(Pageable pageable);
}

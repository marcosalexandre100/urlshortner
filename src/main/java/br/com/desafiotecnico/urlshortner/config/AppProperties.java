package br.com.desafiotecnico.urlshortner.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String baseUrl,
                            String apiKey) {
}

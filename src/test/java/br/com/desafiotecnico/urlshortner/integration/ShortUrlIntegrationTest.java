package br.com.desafiotecnico.urlshortner.integration;


import br.com.desafiotecnico.urlshortner.domain.ShortUrl;
import br.com.desafiotecnico.urlshortner.repository.ShortUrlRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@RequiredArgsConstructor
class ShortUrlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortUrlRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateShortUrlAndGetDetails() throws Exception {
        String requestBody = """
            {
              "originalUrl": "https://www.google.com"
            }
            """;

        MvcResult createResult = mockMvc.perform(post("/v1/urls")
                        .header("X-API-Key", "my-secret-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.originalUrl").value("https://www.google.com"))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(responseBody);
        String id = json.get("id").asText();

        mockMvc.perform(get("/v1/urls/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.originalUrl").value("https://www.google.com"));
    }

    @Test
    void shouldCreateShortUrlAndRedirectToOriginalUrl() throws Exception {
        String requestBody = """
            {
              "originalUrl": "https://www.google.com"
            }
            """;

        MvcResult createResult = mockMvc.perform(post("/v1/urls")
                        .header("X-API-Key", "my-secret-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(responseBody);
        String id = json.get("id").asText();

        mockMvc.perform(get("/v1/urls/redirect/" + id))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.google.com"));
    }

    @Test
    void shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/v1/urls/id-inexistente"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenExpirationDateIsInThePast() throws Exception {
        String requestBody = """
        {
          "originalUrl": "https://www.google.com",
          "expirationDate": "2025-03-07T11:00:00Z"
        }
        """;

        mockMvc.perform(post("/v1/urls")
                        .header("X-API-Key", "my-secret-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenOriginalUrlIsInvalid() throws Exception {
        String requestBody = """
            {
              "originalUrl": "url-invalida"
            }
            """;

        mockMvc.perform(post("/v1/urls")
                        .header("X-API-Key", "my-secret-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

   @Test
   void shouldNotRedirectWhenShortUrlIsExpired() throws Exception{
       ShortUrl shortUrl = new ShortUrl();
       shortUrl.setShortCode("abc123");
       shortUrl.setOriginalUrl("https://www.google.com");
       shortUrl.setCreatedAt(Instant.parse("2022-03-01T10:00:00Z"));
       shortUrl.setExpirationDate(Instant.parse("2026-03-06T10:00:00Z"));
       shortUrl.setClickCount(0L);

       repository.save(shortUrl);

       mockMvc.perform(get("/v1/urls/redirect/abc123"))
               .andExpect(status().isGone());
   }

    @Test
    void shouldReturnUnauthorizedWhenApiKeyMissing() throws Exception {

        String requestBody = """
        {
          "originalUrl": "https://www.google.com"
        }
        """;

        mockMvc.perform(post("/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void shouldListAllUrlsWithPagination() throws Exception {

        String requestBody1 = """
        {
          "originalUrl": "https://www.google.com"
        }
        """;

        String requestBody2 = """
        {
          "originalUrl": "https://www.github.com"
        }
        """;
        mockMvc.perform(post("/v1/urls")
                        .header("X-API-Key", "my-secret-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody1))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/urls")
                        .header("X-API-Key", "my-secret-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody2))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andExpect(jsonPath("$.content[0].originalUrl").exists());
    }

}
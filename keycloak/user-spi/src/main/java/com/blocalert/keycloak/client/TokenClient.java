package com.blocalert.keycloak.client;

import com.blocalert.keycloak.config.EventConfig;
import com.blocalert.keycloak.dto.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class TokenClient {

    private final EventConfig eventConfig;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private String cachedToken;
    private Instant expiresAt;

    public TokenClient(EventConfig eventConfig) {
        this.eventConfig = eventConfig;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String getAccessToken() {

        if(cachedToken != null && expiresAt != null && Instant.now().isBefore(expiresAt.minusSeconds(30)))
            return cachedToken;

        return generateToken();
    }

    private String generateToken() {

        String url = eventConfig.keycloakBaseUrl()
                        + "/realms/"
                        + eventConfig.realm()
                        + "/protocol/openid-connect/token";

        String body =  "grant_type=client_credentials"
                        + "&client_id=" + encode(eventConfig.clientId())
                        + "&client_secret=" + encode(eventConfig.clientSecret());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(
                        "Content-Type",
                        "application/x-www-form-urlencoded"
                )
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();


        try {

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException( "Failed to obtain token. HTTP " + response.statusCode());
            }

            TokenResponse tokenResponse = objectMapper.readValue( response.body(), TokenResponse.class);

            cachedToken = tokenResponse.accessToken();

            expiresAt = Instant.now().plusSeconds(tokenResponse.expiresIn());

            return cachedToken;

        } catch (Exception e) {

            throw new RuntimeException("Unable to obtain access token", e);
        }

    }

    private String encode(String value) {
        return URLEncoder.encode(value,  StandardCharsets.UTF_8);
    }

}
package com.blocalert.keycloak.client;

import com.blocalert.keycloak.config.EventConfig;
import com.blocalert.keycloak.dto.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EventClient {

    private final EventConfig eventConfig;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public EventClient(EventConfig eventConfig) {
        this.eventConfig = eventConfig;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public void sendEvent(UserRequest request, String accessToken) throws Exception {

        String json = objectMapper.writeValueAsString(request);

        String url = eventConfig.userServiceUrl();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response;

        for (int attempt = 1; true; attempt++) {
            try {

                response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                // Success
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    return;
                }

                // Retry only for transient server errors
                if (response.statusCode() == 502 || response.statusCode() == 503 || response.statusCode() == 504) {
                    if (attempt < 3) {
                        Thread.sleep(1000);
                        continue;
                    }
                }

                throw new RuntimeException("Provisioning failed. HTTP "+ response.statusCode() + "\n" + response.body());

            } catch (java.net.ConnectException | java.net.http.HttpTimeoutException e) {

                // Retry connection-related failures
                if (attempt >= 3) {
                    throw e;
                }
                Thread.sleep(1000);
            }
        }

    }
}
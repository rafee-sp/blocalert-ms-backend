package com.blocalert.crypto.service.impl;

import com.blocalert.dto.CryptoDetails;
import com.blocalert.crypto.dto.internal.MarketStatsData;
import com.blocalert.crypto.exception.ExternalApiException;
import com.blocalert.crypto.service.CoingeckoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoingeckoServiceImpl implements CoingeckoService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private static final String CURRENCY = "usd";

    @Retryable(
            includes = {ExternalApiException.class},
            maxRetries = 4,
            delay = 2000,
            multiplier = 2.0
    )
    @Override
    public List<CryptoDetails> fetchCryptoMarketData() {

        log.info("fetchCryptoMarketData called");

        String url = buildCryptoMarketDataUrl();

        CryptoDetails[] response = fetchData(url, CryptoDetails[].class);

        if (response == null || response.length == 0) {
            log.warn("No crypto data returned from Coingecko (URL: {})", url);
            return Collections.emptyList();
        }

        log.info("Fetched {} cryptos from Coingecko", response.length);

        return Arrays.asList(response);
    }

    @Retryable(
            includes = {ExternalApiException.class},
            maxRetries = 4,
            delay = 2000,
            multiplier = 2.0
    )
    public MarketStatsData fetchMarketStats() {

        String url = buildMarketStatsUrl();

        String response = fetchData(url, String.class);

        if (!StringUtils.hasText(response)) {
            log.warn("No market data returned from Coingecko (URL: {})", url);
            throw new ExternalApiException("No market data returned from Coingecko ");
        }

        try {

            JsonNode marketDataNode = objectMapper.readTree(response).path("data");

            return MarketStatsData.map(marketDataNode);

        } catch (RestClientResponseException e) {

            log.error("Coingecko API for fetchMarketStats returned error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalApiException("Coingecko API error: " + e.getStatusCode() + " - " + e.getStatusText());

        } catch (Exception e) {
            log.error("Unexpected error while fetching market data from Coingecko", e);
            throw new ExternalApiException("Unexpected error while fetching market data");
        }
    }

    @Retryable(
            includes = {ExternalApiException.class},
            maxRetries = 4,
            delay = 2000,
            multiplier = 2.0
    )
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCryptoChartData(String cryptoId, String timeframe) {

        log.info("getCryptoChartData called for {} - {}", cryptoId, timeframe);

        String url = buildCryptoChartUrl(cryptoId, timeframe);

        Map<String, Object> response =
                (Map<String, Object>) fetchData(url, Map.class);   // TODO : Use ParameterizedTypeReference

        if (response == null || response.isEmpty()) {
            log.warn("No crypto chart data returned from Coingecko (URL: {})", url);
            throw new ExternalApiException("No market data returned from Coingecko ");
        }

        log.info("Fetched crypto chart data from Coingecko");

        return response;
    }

    private <T> T fetchData(String url, Class<T> responseType) {

        try {
            ResponseEntity<T> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .toEntity(responseType);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Coingecko API returned non-success status: {}", response.getStatusCode());
                throw new ExternalApiException("Coingecko returned non-success status: " + response.getStatusCode());
            }

            return response.getBody();

        } catch (RestClientResponseException e) {

            log.error("Coingecko API returned error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalApiException("Coingecko API error: " + e.getStatusCode() + " - " + e.getStatusText());

        } catch (Exception e) {
            log.error("Unexpected error while fetching crypto data from Coingecko", e);
            throw new ExternalApiException("Unexpected error while fetching crypto data");
        }

    }

    private String buildCryptoMarketDataUrl() {

        return UriComponentsBuilder.newInstance()
                .path("/coins/markets")
                .queryParam("vs_currency", CURRENCY)
                .queryParam("order", "market_cap_desc")
                .queryParam("per_page", 250)  // TODO : Increase
                .queryParam("page", 1)
                .queryParam("sparkline", false)
                .toUriString();
    }

    private String buildMarketStatsUrl() {

        return UriComponentsBuilder.newInstance()
                .path("/global")
                .toUriString();
    }

    private String buildCryptoChartUrl(String cryptoId, String timeframe) {

        return  UriComponentsBuilder.newInstance()
                .pathSegment("coins", cryptoId, "market_chart")
                .queryParam("vs_currency", CURRENCY)
                .queryParam("days", timeframe)
                .toUriString();
    }

}

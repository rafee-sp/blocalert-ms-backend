package com.blocalert.user.domain.assistant.service;

import com.blocalert.dto.AssistantAlertRequest;
import com.blocalert.user.domain.assistant.client.AlertServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertServiceClient alertServiceClient;

    @CircuitBreaker(name = "alertServiceCB", fallbackMethod = "createAlertFallback")
    @Retry(name = "alertServiceCB")
    public Long createAlert(AssistantAlertRequest request) {

        log.info("createAlert called for {} for {}", request.userId(), request.cryptoId());
        return alertServiceClient.createAlertFromAssistant(request);
    }

    public Long createAlertFallback(AssistantAlertRequest request, Throwable t) {
        log.error("Error creating alert via assistant {}", t.getMessage());
        return null;
    }
}

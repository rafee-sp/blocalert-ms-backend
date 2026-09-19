package com.blocalert.alert.service.impl;

import com.blocalert.alert.client.CryptoServiceClient;
import com.blocalert.alert.service.CryptoService;
import com.blocalert.dto.CryptoPrice;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoServiceImpl implements CryptoService {

    private final CryptoServiceClient cryptoServiceClient;

    @Override
    @CircuitBreaker(name = "cryptoServiceCB", fallbackMethod = "getCryptoPriceDetailsFallback")
    @Retry(name = "cryptoServiceCB")
    public Map<String, CryptoPrice> getCryptoPriceDetails(Set<String> cryptoIds) {
        log.debug("getCryptoPriceDetails called for {}", cryptoIds.size());
        return cryptoServiceClient.getCryptoDataByIds(cryptoIds);
    }

    private Map<String, CryptoPrice> getCryptoPriceDetailsFallback(Set<String> cryptoIds, Throwable throwable) {
        log.error("crypto-service unavailable while resolving price details for {} ids, skipping this evaluation pass",
                cryptoIds.size(), throwable);
        return Collections.emptyMap();
    }
}

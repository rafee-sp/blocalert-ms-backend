package com.blocalert.crypto.scheduler;

import com.blocalert.crypto.service.CryptoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CryptoScheduler {  // TODO: DO LOCK

    private final CryptoService cryptoService;

    @Scheduled(fixedDelay = 30000, initialDelay = 2000)
    public void fetchCryptoData() {
        log.info("fetchCryptoData scheduler started at : {}", LocalDateTime.now());
        cryptoService.fetchAndCacheCryptoData();
    }

    @Scheduled(fixedRate = 30 * 60 * 1000, initialDelay = 10000)
    public void fetchMarketStats() {
        log.info("fetchMarketStats scheduler started at : {}", LocalDateTime.now());
        cryptoService.fetchAndCacheMarketStats();
    }

}

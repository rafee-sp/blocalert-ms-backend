package com.blocalert.user.domain.assistant.service;

import com.blocalert.dto.CryptoDetails;
import com.blocalert.dto.HistorySummary;
import com.blocalert.dto.TopMovers;
import com.blocalert.user.domain.assistant.client.CryptoServiceClient;
import com.blocalert.user.domain.assistant.dto.CryptoDetailResult;
import com.blocalert.user.domain.assistant.dto.HistorySummaryResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoService {

    private final CryptoServiceClient cryptoClient;

    @CircuitBreaker(name = "cryptoServiceCB", fallbackMethod = "getCryptoDetailsFallback")
    @Retry(name = "cryptoServiceCB")
    public CryptoDetailResult getCryptoDetails(String cryptoQuery) {

        log.info("getCryptoDetails called for {}", cryptoQuery);
        CryptoDetails cryptoDetails = cryptoClient.getCryptoDetails(cryptoQuery);

        if(cryptoDetails == null)
            return CryptoDetailResult.notFound();

        return CryptoDetailResult.of(cryptoDetails);
    }

    @CircuitBreaker(name = "cryptoServiceCB", fallbackMethod = "getTopMoversFallback")
    @Retry(name = "cryptoServiceCB")
    public List<TopMovers> getTopMovers(String metic, boolean descending, Double minAbsoluteChange, int limit) {

        log.info("getTopMovers called for metic - {}, descending - {}, minAbsoluteChange - {}, limit - {}", metic, descending, minAbsoluteChange, limit);

        limit = Math.min(10, limit);

        minAbsoluteChange = Optional.ofNullable(minAbsoluteChange)
                .filter(v -> v >= 0)
                .orElse(null);

        return cryptoClient.getTopMovers(metic, descending, minAbsoluteChange, limit);
    }

    @CircuitBreaker(name = "cryptoServiceCB", fallbackMethod = "getHistorySummaryFallback")
    @Retry(name = "cryptoServiceCB")
    public HistorySummaryResult getHistorySummary(String coinQuery, String timeframe) {

        log.info("getHistorySummary called for {} - {}", coinQuery, timeframe);

        HistorySummary summary = cryptoClient.getHistorySummary(coinQuery, timeframe);

        if(summary == null)
            return HistorySummaryResult.notFound();

        return HistorySummaryResult.of(summary);
    }

    public CryptoDetailResult getCryptoDetailsFallback(String cryptoQuery, Throwable t) {
        log.error("Error in getCryptoDetailsFallback {}", t.getMessage());
        return CryptoDetailResult.unavailable();
    }

    public HistorySummaryResult getHistorySummaryFallback(String coinQuery, String timeframe, Throwable t) {
        log.error("Error in getHistorySummaryFallback {}", t.getMessage());
        return HistorySummaryResult.unavailable();
    }

    public List<TopMovers> getTopMoversFallback(String metic, boolean descending, Double minAbsoluteChange, int limit, Throwable t) {
        log.error("Error in getTopMoversFallback {}", t.getMessage());
        return List.of();
    }


}

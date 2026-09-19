package com.blocalert.crypto.service;

import com.blocalert.dto.CryptoDetails;
import com.blocalert.crypto.dto.internal.CryptoSummary;
import com.blocalert.crypto.dto.internal.MarketStatsData;
import com.blocalert.dto.CryptoPrice;
import com.blocalert.dto.HistorySummary;
import com.blocalert.dto.TopMovers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CryptoService {
    
    void fetchAndCacheCryptoData();

    void fetchAndCacheMarketStats();

    List<CryptoSummary> getCachedCryptoData();

    MarketStatsData getCachedMarketStats();

    CryptoDetails getCryptoDetail(String cryptoId);

    List<CryptoSummary> searchCrypto(String searchTerm);

    Map<String, Object> getChartData(String id, String timeframe);

    Map<String, BigDecimal> getCryptoPrice(String cryptoId);

    Map<String, CryptoPrice> getCryptosDetailByIds(Set<String> cryptoIds);

    CryptoDetails validateAndGetCrypto(String cryptoId);

    HistorySummary getHistorySummary(String coinQuery, String timeframe);

    List<TopMovers> getTopMovers(String metic, boolean descending, Double minAbsoluteChange, int limit);

}

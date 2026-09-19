package com.blocalert.crypto.service;

import com.blocalert.dto.CryptoDetails;
import com.blocalert.crypto.dto.internal.MarketStatsData;
import java.util.List;
import java.util.Map;

public interface CoingeckoService {

    List<CryptoDetails> fetchCryptoMarketData();

    MarketStatsData fetchMarketStats();

    Map<String, Object> getCryptoChartData(String cryptoId, String timeframe);
}

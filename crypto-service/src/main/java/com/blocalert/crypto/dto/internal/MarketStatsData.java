package com.blocalert.crypto.dto.internal;

import tools.jackson.databind.JsonNode;

public record MarketStatsData (

    Integer totalCoins,
    Integer totalExchanges,
    Long totalMarketCap,
    Double marketCapChange24h,
    Long volume24h,
    Double btcDominance,
    Double ethDominance
){
    public static MarketStatsData map(JsonNode marketData) {

        return new MarketStatsData(
                marketData.path("active_cryptocurrencies").asInt(0),
                marketData.path("markets").asInt(0),
                marketData.path("total_market_cap").path("usd").asLong(0L),
                marketData.path("market_cap_change_percentage_24h_usd").asDouble(0.0),
                marketData.path("total_volume").path("usd").asLong(0L),
                marketData.path("market_cap_percentage").path("btc").asDouble(0.0),
                marketData.path("market_cap_percentage").path("eth").asDouble(0.0)
        );
    }
}

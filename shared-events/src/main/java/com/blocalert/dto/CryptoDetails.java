package com.blocalert.dto;

import java.math.BigDecimal;

public record CryptoDetails(
    String id,
    String symbol,
    String name,
    String image,
    BigDecimal current_price,
    Long market_cap,
    Integer market_cap_rank,
    Long circulating_supply,
    Double price_change_percentage_24h,
    Long fully_diluted_valuation,
    Long total_supply,
    Double high_24h,
    Double low_24h,
    Double market_cap_change_percentage_24h,
    Long max_supply,
    Double ath,
    Double atl,
    String atl_date,
    String ath_date
) {
}

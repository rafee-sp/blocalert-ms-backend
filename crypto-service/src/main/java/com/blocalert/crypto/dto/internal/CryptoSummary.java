package com.blocalert.crypto.dto.internal;

import com.blocalert.dto.CryptoDetails;

import java.math.BigDecimal;
import java.util.List;

public record CryptoSummary(
        String id,
        String symbol,
        String name,
        String image,
        BigDecimal current_price,
        Long market_cap,
        Integer market_cap_rank,
        Long circulating_supply,
        Double price_change_percentage_24h

) {

    public static List<CryptoSummary> mapToSummary(List<CryptoDetails> cryptoList) {

        return cryptoList.stream().map(c -> new CryptoSummary(
                c.id(),
                c.symbol(),
                c.name(),
                c.image(),
                c.current_price(),
                c.market_cap(),
                c.market_cap_rank(),
                c.circulating_supply(),
                c.price_change_percentage_24h()
        )).toList();

    }

}

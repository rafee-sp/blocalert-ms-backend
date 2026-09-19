package com.blocalert.dto;

import java.math.BigDecimal;

public record CryptoPrice(
        String id,
        String symbol,
        String name,
        String image,
        BigDecimal current_price
) {
}

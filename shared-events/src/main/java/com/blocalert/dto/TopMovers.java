package com.blocalert.dto;

import java.math.BigDecimal;

public record TopMovers(
        String id,
        String name,
        String symbol,
        BigDecimal currentPrice,
        Double change24h
) {}

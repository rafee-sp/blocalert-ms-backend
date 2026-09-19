package com.blocalert.dto;

public record HistorySummary(
        String timeframe,
        Double startPrice,
        Double endPrice,
        Double periodHigh,
        Double periodLow,
        Double percentChange
) {
}

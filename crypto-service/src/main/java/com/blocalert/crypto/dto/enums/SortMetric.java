package com.blocalert.crypto.dto.enums;

public enum SortMetric {
    PRICE_CHANGE_24H,
    MARKET_CAP,
    MARKET_CAP_RANK;

    public static SortMetric of(String metric) {
        try {
            return SortMetric.valueOf(metric);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid metric found "+ metric);
        }
    }

}

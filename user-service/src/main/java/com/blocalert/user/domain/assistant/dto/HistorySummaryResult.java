package com.blocalert.user.domain.assistant.dto;

import com.blocalert.dto.HistorySummary;

public record HistorySummaryResult(
        boolean available,
        boolean found,
        HistorySummary historySummary
) {

    public static HistorySummaryResult notFound() {
        return new HistorySummaryResult( false, true, null);
    }

    public static HistorySummaryResult unavailable() {
        return new HistorySummaryResult(true, false, null);
    }

    public static HistorySummaryResult of(HistorySummary summary) {
        return new HistorySummaryResult(true, true, summary);
    }
}

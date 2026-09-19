package com.blocalert.crypto.events.event;

import com.blocalert.crypto.dto.internal.MarketStatsData;

public record MarketStatsUpdatedEvent(
        MarketStatsData marketStats
) {
}
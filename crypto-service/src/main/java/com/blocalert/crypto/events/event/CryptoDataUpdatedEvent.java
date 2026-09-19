package com.blocalert.crypto.events.event;

import com.blocalert.dto.CryptoDetails;
import com.blocalert.crypto.dto.internal.CryptoSummary;
import java.util.List;
import java.util.Map;

public record CryptoDataUpdatedEvent(
        Map<String, CryptoDetails> cryptoDetails,
        List<CryptoSummary> cryptoSummaries
) {
}
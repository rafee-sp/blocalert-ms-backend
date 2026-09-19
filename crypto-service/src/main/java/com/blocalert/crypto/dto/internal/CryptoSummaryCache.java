package com.blocalert.crypto.dto.internal;

import java.util.List;

public record CryptoSummaryCache(
        List<CryptoSummary> cryptoSummaryList
) {
}

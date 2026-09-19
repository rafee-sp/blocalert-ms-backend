package com.blocalert.user.domain.assistant.dto;

import com.blocalert.dto.CryptoDetails;

public record CryptoDetailResult(
        CryptoDetails cryptoDetails,
        boolean found,
        boolean available
) {

    public static CryptoDetailResult notFound() {
        return new CryptoDetailResult(null, false, true);
    }

    public static CryptoDetailResult unavailable() {
        return new CryptoDetailResult(null, true, false);
    }

    public static CryptoDetailResult of(CryptoDetails cryptoDetails) {
        return new CryptoDetailResult(cryptoDetails, true, true);
    }

}

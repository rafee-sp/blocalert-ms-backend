package com.blocalert.event;


import com.blocalert.dto.CryptoPrice;

import java.util.List;

public record CryptoPriceEvent(
        List<CryptoPrice> cryptoPrices
) {
}

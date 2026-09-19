package com.blocalert.alert.service;

import com.blocalert.dto.CryptoPrice;
import java.util.Map;
import java.util.Set;

public interface CryptoService {

    Map<String, CryptoPrice> getCryptoPriceDetails(Set<String> cryptoIds);
}

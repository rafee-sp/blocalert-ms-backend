package com.blocalert.alert.client;

import com.blocalert.dto.CryptoPrice;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import java.util.Map;
import java.util.Set;

@HttpExchange
public interface CryptoServiceClient {

    @GetExchange("/api/cryptos/details")
    Map<String, CryptoPrice> getCryptoDataByIds(@RequestParam Set<String> cryptoIds);
}

package com.blocalert.user.domain.assistant.client;

import com.blocalert.dto.CryptoDetails;
import com.blocalert.dto.HistorySummary;
import com.blocalert.dto.TopMovers;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange
public interface CryptoServiceClient {

    @GetExchange("/api/cryptos/resolve/details")
    CryptoDetails getCryptoDetails(@RequestParam String cryptoQuery);

    @GetExchange("/api/cryptos/top-movers")
    List<TopMovers> getTopMovers(@RequestParam String metic,
                                 @RequestParam boolean descending,
                                 @RequestParam Double minAbsoluteChange,
                                 @RequestParam int limit);

    @GetExchange("/api/cryptos/history-summary")
    HistorySummary getHistorySummary(@RequestParam String coinQuery,
                                     @RequestParam String timeframe);
}

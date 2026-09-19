package com.blocalert.crypto.controller;

import com.blocalert.crypto.dto.internal.CryptoSummary;
import com.blocalert.crypto.service.CryptoService;
import com.blocalert.dto.CryptoDetails;
import com.blocalert.dto.CryptoPrice;
import com.blocalert.dto.HistorySummary;
import com.blocalert.dto.TopMovers;
import com.blocalert.dto.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.GetExchange;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/cryptos")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CryptoController {

    private final CryptoService cryptoService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchCrypto(@RequestParam
                                                    @NotBlank(message = "Search term is required")
                                                    String searchTerm){

        log.info("searchCrypto called");

        List<CryptoSummary> cryptoSummaryList = cryptoService.searchCrypto(searchTerm);

        return ResponseEntity.ok().body(new ApiResponse("Search success", cryptoSummaryList));
    }

    @GetMapping("/{id}/chart")
    public ResponseEntity<ApiResponse> getChartData(@PathVariable String id,
                                                    @RequestParam @NotBlank(message = "Timeframe is required")
                                                    String timeframe){

        log.info("getChartData called");

        Map<String, Object> chartDataMap = cryptoService.getChartData(id, timeframe);

        return ResponseEntity.ok().body(new ApiResponse("Chart data fetched", chartDataMap));
    }

    @GetMapping("/price")
    public ResponseEntity<ApiResponse> getCryptoPrice(@RequestParam
                                                      @NotBlank(message = "CryptoId is required")
                                                      String cryptoId){

        log.info("getCryptoPrice called");

        Map<String, BigDecimal> cryptoPrice = cryptoService.getCryptoPrice(cryptoId);
        return ResponseEntity.ok().body(new ApiResponse("Crypto price fetched", cryptoPrice));
    }

    @GetMapping("/details")
    Map<String, CryptoPrice> getCryptoDataByIds(@RequestParam
                                                @NotNull(message = "CryptoIds cannot be null")
                                                Set<String> cryptoIds){
        log.info("getCryptoDataByIds called");
        return cryptoService.getCryptosDetailByIds(cryptoIds);
    }

    @GetMapping("/resolve/details")
    CryptoDetails getCryptoDetail(@RequestParam
                                  @NotNull(message = "Crypto Query value cannot be null")
                                  String cryptoQuery){
        log.info("getCryptoDetail called");
        return cryptoService.validateAndGetCrypto(cryptoQuery);
    }

    @GetExchange("/history-summary")
    HistorySummary getHistorySummary(@RequestParam String coinQuery,
                                     @RequestParam String timeframe){
        log.info("CryptoController(getHistorySummary) called");
        return cryptoService.getHistorySummary(coinQuery, timeframe);
    }

    @GetMapping("/top-movers")
    List<TopMovers> getTopMovers(@RequestParam String metic,
                                 @RequestParam(defaultValue = "true") boolean descending,
                                 @RequestParam(required = false) Double minAbsoluteChange,
                                 @RequestParam(defaultValue = "5") int limit){

        log.info("CryptoController(getTopMovers) called");
        return cryptoService.getTopMovers(metic, descending, minAbsoluteChange, limit);
    }

}

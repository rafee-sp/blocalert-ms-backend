package com.blocalert.user.domain.assistant.tools;

import com.blocalert.dto.TopMovers;
import com.blocalert.user.domain.assistant.dto.CryptoDetailResult;
import com.blocalert.user.domain.assistant.dto.HistorySummaryResult;
import com.blocalert.user.domain.assistant.service.CryptoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CryptoTool {

    private final CryptoService cryptoService;

    @Tool(description = ChatPrompt.CRYPTO_DETAIL_PROMPT)
    public CryptoDetailResult getCryptoData(
            @ToolParam(description = ChatPrompt.COIN_QUERY_PROMPT) String coinQuery
    ) {
        log.info("getCryptoData tool called");
        return cryptoService.getCryptoDetails(coinQuery);
    }

    @Tool(description = ChatPrompt.CRYPTO_HISTORY)
    public HistorySummaryResult getHistorySummary(
            @ToolParam(description = ChatPrompt.COIN_QUERY_PROMPT) String coinQuery,
            @ToolParam(description = ChatPrompt.TIMEFRAME) String timeframe
    ){
        log.info("getHistorySummary tool called");
        return cryptoService.getHistorySummary(coinQuery, timeframe);
    }

    @Tool(description = ChatPrompt.TOP_MOVERS_PROMPT)
    public List<TopMovers> getTopMovers(
            @ToolParam(description = ChatPrompt.CRYPTO_FILTER_METRIC) String metic,
            @ToolParam(description = ChatPrompt.CRYPTO_ORDERING) boolean descending,
            @ToolParam(description = ChatPrompt.ABSOLUTE_CHANGE) Double minAbsoluteChange,
            @ToolParam(description = ChatPrompt.CRYPTO_LIMIT) int limit
    ){
        log.info("getTopMovers tool called");
        return cryptoService.getTopMovers(metic, descending, minAbsoluteChange, limit);
    }

}

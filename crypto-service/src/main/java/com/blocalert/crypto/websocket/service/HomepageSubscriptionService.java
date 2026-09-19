package com.blocalert.crypto.websocket.service;

import com.blocalert.crypto.dto.internal.CryptoSummary;
import com.blocalert.crypto.dto.internal.MarketStatsData;
import com.blocalert.crypto.dto.internal.PageDTO;
import com.blocalert.crypto.dto.response.CryptoResponse;
import com.blocalert.crypto.service.CryptoService;
import com.blocalert.crypto.websocket.common.CryptoPagePayloadBuilder;
import com.blocalert.crypto.websocket.common.MessageSender;
import com.blocalert.crypto.websocket.dto.ErrorPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomepageSubscriptionService {

    private final CryptoService cryptoService;
    private final HomepageSubscriptionRegistry subscriptionRegistry;
    private final CryptoPagePayloadBuilder payloadBuilder;
    private final MessageSender sender;

    public void subscribeToPage(String sessionId, PageDTO pageDTO) {

        if (!isValidPageRequest(pageDTO.page(), pageDTO.size())) {
            sendError(sessionId, "Invalid page or size");
            return;
        }

        List<CryptoSummary> cryptoSummaryList = cryptoService.getCachedCryptoData();
        int totalPages = Math.max(1, (int) Math.ceil((double) cryptoSummaryList.size() / pageDTO.size()));
        int page = Math.min(pageDTO.page(), totalPages);

        PageDTO resolved = new PageDTO(page, pageDTO.size());
        subscriptionRegistry.set(sessionId, resolved);

        CryptoResponse response = payloadBuilder.buildPage(resolved, cryptoSummaryList);
        sender.sendCryptoHomeMessage(sessionId, "/queue/crypto/page", response);
    }

    public void subscribeToMarketData(String sessionId) {
        MarketStatsData marketStatsData = cryptoService.getCachedMarketStats();
        sender.sendTopicMessage("/topic/market-data", marketStatsData);
    }

    public void removeSubscription(String sessionId) {
        subscriptionRegistry.remove(sessionId);
    }

    private void sendError(String sessionId, String message) {
        sender.sendCryptoHomeMessage(sessionId, "/queue/errors", new ErrorPayload(message));
    }

    private boolean isValidPageRequest(int page, int size) {
        return page > 0 && size > 0 && size <= 100;
    }

}
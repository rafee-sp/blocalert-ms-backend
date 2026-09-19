package com.blocalert.crypto.websocket.service;

import com.blocalert.crypto.dto.internal.CryptoSummary;
import com.blocalert.crypto.dto.internal.MarketStatsData;
import com.blocalert.crypto.dto.response.CryptoResponse;
import com.blocalert.crypto.websocket.common.CryptoPagePayloadBuilder;
import com.blocalert.crypto.websocket.common.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomepageBroadcastService {

    private final HomepageSubscriptionRegistry subscriptionRegistry;
    private final CryptoPagePayloadBuilder payloadBuilder;
    private final MessageSender sender;

    public void broadcastCryptoUpdates(List<CryptoSummary> cryptoSummaryList) {
        subscriptionRegistry.getAll().forEach((sessionId, pageDTO) -> {
            try {
                CryptoResponse response = payloadBuilder.buildPage(pageDTO, cryptoSummaryList);
                sender.sendCryptoHomeMessage(sessionId, "/queue/crypto/page", response);
            } catch (Exception e) {
                log.error("Exception broadcasting to session {}: {}", sessionId, e.getMessage(), e);
            }
        });
    }

    public void broadcastMarketStatsUpdates(MarketStatsData marketStatsData) {
        sender.sendTopicMessage("/topic/market-data", marketStatsData);
    }
}

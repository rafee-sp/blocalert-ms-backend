package com.blocalert.crypto.events.listener;

import com.blocalert.crypto.events.event.CryptoDataUpdatedEvent;
import com.blocalert.crypto.events.event.MarketStatsUpdatedEvent;
import com.blocalert.crypto.websocket.service.CryptoDetailBroadcastService;
import com.blocalert.crypto.websocket.service.HomepageBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CryptoEventListener  {

    private final HomepageBroadcastService homepageBroadcastService;
    private final CryptoDetailBroadcastService cryptoDetailBroadcastService;

    @EventListener
    public void handleCryptoDataUpdated(CryptoDataUpdatedEvent event) {
        cryptoDetailBroadcastService.broadcastCryptoDetailUpdates(event.cryptoDetails());
        homepageBroadcastService.broadcastCryptoUpdates(event.cryptoSummaries());
    }

    @EventListener
    public void handleMarketStatsUpdated(MarketStatsUpdatedEvent event) {
        homepageBroadcastService.broadcastMarketStatsUpdates(event.marketStats());
    }

}

package com.blocalert.crypto.websocket.service;

import com.blocalert.dto.CryptoDetails;
import com.blocalert.crypto.websocket.common.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoDetailBroadcastService {

    private final CryptoDetailSubscriptionRegistry subscriptionRegistry;
    private final MessageSender sender;

    public void broadcastCryptoDetailUpdates(Map<String, CryptoDetails> cryptoDetailsMap) {

        log.info("broadcastCryptoDetailUpdates to {} users at {}", subscriptionRegistry.getAll().size(), LocalDateTime.now());

        subscriptionRegistry.getAll().forEach((userId, symbols) -> {
            for (String symbol : symbols) {
                try {
                    CryptoDetails detail = cryptoDetailsMap.get(symbol);

                    if (detail == null) {
                        log.warn("No crypto data found for {} skipping user {}", symbol, userId);
                        continue;
                    }
                    log.info("sending data for {} user {}", symbol, userId);
                    sender.sendCryptoDetailMessage(userId, "/queue/crypto/detail", detail);
                } catch (Exception e) {
                    log.error("Exception occurred at broadcastCryptoDetailUpdates for user {}: {}", userId, e.getMessage(), e);
                }
            }
        });
    }
}
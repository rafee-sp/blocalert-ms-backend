package com.blocalert.crypto.websocket.service;

import com.blocalert.dto.CryptoDetails;
import com.blocalert.crypto.service.CryptoService;
import com.blocalert.crypto.websocket.common.MessageSender;
import com.blocalert.crypto.websocket.dto.ErrorPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoDetailSubscriptionService {

    private final CryptoService cryptoService;
    private final CryptoDetailSubscriptionRegistry subscriptionRegistry;
    private final MessageSender sender;

    public void subscribe(String userId, String symbol) {
        if (symbol == null || symbol.isBlank()) {
            sendError(userId, "Invalid symbol");
            return;
        }

        subscriptionRegistry.add(userId, symbol);

        CryptoDetails detail = cryptoService.getCryptoDetail(symbol);

        if (detail != null)
            sender.sendCryptoDetailMessage(userId, "/queue/crypto/detail", detail);
    }

    public void unsubscribe(String userId, String symbol) {
        subscriptionRegistry.remove(userId, symbol);
    }

    public void removeAllSubscriptions(String userId) {
        subscriptionRegistry.removeAll(userId);
    }

    private void sendError(String userId, String message) {
        sender.sendCryptoHomeMessage(userId, "/queue/errors", new ErrorPayload(message));
    }
}
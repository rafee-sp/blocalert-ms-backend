package com.blocalert.crypto.websocket.controller;

import com.blocalert.crypto.websocket.dto.SymbolSubscriptionRequest;
import com.blocalert.crypto.websocket.service.CryptoDetailSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CryptoWebsocketController {

    private final CryptoDetailSubscriptionService subscriptionService;

    @MessageMapping("/crypto/detail/subscribe")
    public void subscribe(@Payload SymbolSubscriptionRequest request, Principal principal) {
        log.info("subscribe called, principal={}", principal.getName());
        subscriptionService.subscribe(principal.getName(), request.symbol());
    }

    @MessageMapping("/crypto/detail/unsubscribe")
    public void unsubscribe(@Payload SymbolSubscriptionRequest request, Principal principal) {
        subscriptionService.unsubscribe(principal.getName(), request.symbol());
    }

}


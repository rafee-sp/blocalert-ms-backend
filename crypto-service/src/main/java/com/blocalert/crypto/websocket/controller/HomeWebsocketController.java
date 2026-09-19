package com.blocalert.crypto.websocket.controller;

import com.blocalert.crypto.dto.internal.PageDTO;
import com.blocalert.crypto.websocket.service.HomepageSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class HomeWebsocketController {

    private final HomepageSubscriptionService subscriptionService;

    @MessageMapping("/crypto/page/subscribe")
    public void subscribeToPage(@Payload PageDTO pageDTO, StompHeaderAccessor accessor) {
        subscriptionService.subscribeToPage(accessor.getSessionId(), pageDTO);
    }

    @MessageMapping("/market-data/subscribe")
    public void subscribeToMarketData(StompHeaderAccessor accessor) {
        subscriptionService.subscribeToMarketData(accessor.getSessionId());
    }
}
package com.blocalert.crypto.websocket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class WebsocketDisconnectListener {

    private final CryptoDetailSubscriptionService cryptoDetailService;
    private final HomepageSubscriptionService homepageService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        boolean requiresAuth = accessor.getSessionAttributes() != null
                && Boolean.TRUE.equals(accessor.getSessionAttributes().get("requiresAuth"));

        if (!requiresAuth) {
            homepageService.removeSubscription(accessor.getSessionId());
        } else {
            Principal principal = accessor.getUser();
            if (principal != null) {
                cryptoDetailService.removeAllSubscriptions(principal.getName());
            }
        }
    }
}
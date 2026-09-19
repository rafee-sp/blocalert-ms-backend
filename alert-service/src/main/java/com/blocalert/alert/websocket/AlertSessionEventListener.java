package com.blocalert.alert.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertSessionEventListener {

    private final AlertSessionRegistry sessionRegistry;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal != null) {
            sessionRegistry.addSession(Long.valueOf(principal.getName()), accessor.getSessionId());
            log.info("WebSocket authenticated: userId={}, sessionId={}", principal.getName(), accessor.getSessionId());
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal != null) {
            sessionRegistry.removeSession(Long.valueOf(principal.getName()), accessor.getSessionId());
            log.debug("WebSocket session cleaned up: {}", accessor.getSessionId());
        }
    }
}
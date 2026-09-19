package com.blocalert.alert.websocket;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertSessionShutdownHandler {

    private final AlertSessionRegistry sessionRegistry;

    @PreDestroy
    public void clearSessions() {
        log.info("Clearing WebSocket alert sessions: users={}", sessionRegistry.userCount());
        sessionRegistry.clear();  // By default, websocket sessions will be closed
    }
}
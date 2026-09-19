package com.blocalert.alert.events.listener;

import com.blocalert.alert.websocket.AlertWebsocketService;
import com.blocalert.event.AlertNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertWebsocketListener{

    private final AlertWebsocketService alertWebsocketService;

    @Async
    @EventListener
    public void publishAlertNotifications(AlertNotificationEvent alertEvent) {
        alertWebsocketService.broadcastAlerts(alertEvent);
    }
}
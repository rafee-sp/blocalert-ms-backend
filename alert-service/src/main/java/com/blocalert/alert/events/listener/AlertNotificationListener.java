package com.blocalert.alert.events.listener;

import com.blocalert.event.AlertNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertNotificationListener {

    private final StreamBridge streamBridge;

    @EventListener
    public void sendNotification(AlertNotificationEvent event) {
        streamBridge.send("alertNotification-out-0", event);
    }
}

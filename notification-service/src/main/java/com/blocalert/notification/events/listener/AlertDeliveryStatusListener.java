package com.blocalert.notification.events.listener;

import com.blocalert.event.AlertDeliveryStatusBatchEvent;
import com.blocalert.event.AlertDeliveryStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertDeliveryStatusListener {

    private final StreamBridge streamBridge;

    @EventListener
    public void sendAlertDeliveryStatus(AlertDeliveryStatusEvent event) {
        streamBridge.send("alertDelivery-out-0", event);
    }

    @EventListener
    public void sendBatchAlertDeliveryStatus(AlertDeliveryStatusBatchEvent event) {
        streamBridge.send("alertBatchDelivery-out-0", event);
    }
}

package com.blocalert.user.events.listener;

import com.blocalert.event.SubscriptionNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionNotificationEventListener {

    private final StreamBridge streamBridge;

    @EventListener
    public void handleSubscriptionNotificationEvent(SubscriptionNotificationEvent event) {
        streamBridge.send("subscriptionNotification-out-0", event);
    }
}

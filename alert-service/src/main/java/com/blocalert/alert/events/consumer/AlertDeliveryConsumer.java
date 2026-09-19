package com.blocalert.alert.events.consumer;

import com.blocalert.alert.service.AlertDeliveryService;
import com.blocalert.event.AlertDeliveryStatusBatchEvent;
import com.blocalert.event.AlertDeliveryStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertDeliveryConsumer {

    private final AlertDeliveryService alertDeliveryService;

    @Bean
    public Consumer<AlertDeliveryStatusEvent> alertDelivery() {
        return event -> alertDeliveryService
                                .upsertDelivery(event.alertDeliveryStatus());
    }

    @Bean
    public Consumer<AlertDeliveryStatusBatchEvent> alertBatchDelivery() {
        return event -> alertDeliveryService
                                .recordAlertDeliveries(event.alertDeliveryStatusList());
    }
}

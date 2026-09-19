package com.blocalert.alert.events.consumer;

import com.blocalert.alert.service.AlertService;
import com.blocalert.event.CryptoPriceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoPriceConsumer {

    private final AlertService alertService;

    @Bean
    public Consumer<CryptoPriceEvent> consumePrice() {
        return event -> {
            log.info("Received crypto price event: {}", event);
            alertService.evaluateAndPublishAlerts(event);
        };
    }
}

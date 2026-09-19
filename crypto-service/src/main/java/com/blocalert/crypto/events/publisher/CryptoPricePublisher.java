package com.blocalert.crypto.events.publisher;

import com.blocalert.crypto.events.event.CryptoPriceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoPricePublisher {

    private final StreamBridge streamBridge;

    public void publishEvent(CryptoPriceEvent cryptoPriceEvent) {
        log.debug("pushed consume price event");
        streamBridge.send("consumePrice-out-0", cryptoPriceEvent);
    }

}

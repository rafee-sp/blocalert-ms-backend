package com.blocalert.alert.service.impl;

import com.blocalert.alert.entity.Alert;
import com.blocalert.alert.entity.AlertDelivery;
import com.blocalert.alert.repository.AlertDeliveryRepository;
import com.blocalert.alert.service.AlertDeliveryService;
import com.blocalert.alert.service.AlertService;
import com.blocalert.dto.AlertDeliveryStatus;
import com.blocalert.enums.AlertChannelStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertDeliveryServiceImpl implements AlertDeliveryService {

    private final AlertDeliveryRepository alertDeliveryRepository;
    private final AlertService alertService;

    @Transactional
    @Override
    public void recordAlertDeliveries(List<AlertDeliveryStatus> deliveryResults) {

        log.info("recordWebsocketDeliveries called with {} results", deliveryResults.size());

        List<Long> alertIds = deliveryResults.stream()
                .map(AlertDeliveryStatus::alertId)
                .distinct()
                .toList();

        Map<Long, Alert> alertMap = alertService.getAlertsByIds(alertIds)
                .stream()
                .collect(Collectors.toMap(Alert::getId, Function.identity()));

        List<AlertDelivery> alertDeliveryList = new ArrayList<>();

        for (AlertDeliveryStatus result : deliveryResults) {

            Alert alert = alertMap.get(result.alertId());

            if (alert == null) {
                log.warn("Alert is null for alertId : {}", result.alertId());
                continue;
            }

            AlertDelivery alertDelivery = new AlertDelivery();
            alertDelivery.setAlert(alert);
            alertDelivery.setAlertChannel(result.channel());
            alertDelivery.setTriggeredAt(result.triggeredAt());
            alertDelivery.setAlertStatus(result.status());

            if (result.status() == AlertChannelStatus.DELIVERED)
                alertDelivery.setDeliveredAt(result.deliveredAt());

            alertDeliveryList.add(alertDelivery);
        }

        if (!alertDeliveryList.isEmpty()) {
            alertDeliveryRepository.saveAll(alertDeliveryList);
        } else {
            log.warn("No alert deliver entities found");
        }

        log.info("delivery records batch inserted {}", alertDeliveryList.size());
    }

    @Override
    public void upsertDelivery(AlertDeliveryStatus alertDeliveryStatus) {
        alertDeliveryRepository.upsertDelivery(alertDeliveryStatus.alertId(), alertDeliveryStatus.channel(), alertDeliveryStatus.status(), alertDeliveryStatus.triggeredAt(), alertDeliveryStatus.deliveredAt());
    }
}

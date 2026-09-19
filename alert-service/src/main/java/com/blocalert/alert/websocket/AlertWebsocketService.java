package com.blocalert.alert.websocket;

import com.blocalert.alert.service.AlertDeliveryService;
import com.blocalert.alert.service.AlertService;
import com.blocalert.dto.AlertDeliveryStatus;
import com.blocalert.dto.TriggeredAlert;
import com.blocalert.enums.AlertChannel;
import com.blocalert.enums.AlertChannelStatus;
import com.blocalert.event.AlertNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertWebsocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AlertSessionRegistry sessionRegistry;
    private final AlertService alertService;
    private final AlertDeliveryService alertDeliveryService;

    public void broadcastAlerts(AlertNotificationEvent event) {

        log.info("broadcastAlerts to {} users at {}", sessionRegistry.userCount(), LocalDateTime.now());

        try {
            List<TriggeredAlert> triggeredAlerts = event.alertList();

            if (triggeredAlerts == null || triggeredAlerts.isEmpty()) {
                log.debug("No triggered alerts to broadcast");
                return;
            }

            Map<Long, List<TriggeredAlert>> alertsByUser = triggeredAlerts.stream()
                    .collect(Collectors.groupingBy(TriggeredAlert::userId));

            List<AlertDeliveryStatus> deliveryResults = new ArrayList<>();

            alertsByUser.forEach((userId, userAlerts) -> {

                boolean delivered = deliverToUser(userId, userAlerts);
                buildDeliveryStatus(deliveryResults, userAlerts, delivered);
            });

            recordDeliveryResults(triggeredAlerts, deliveryResults);

        } catch (Exception e) {
            log.error("Error occurred in broadcastAlerts", e);
        }
    }

    private boolean deliverToUser(Long userId, List<TriggeredAlert> userAlerts) {

        Set<String> sessions = sessionRegistry.getSessions(userId);

        if (sessions.isEmpty()) {
            log.debug("No active WebSocket sessions for user {}", userId);
            return false;
        }

        try {
            log.debug("Sending {} alerts to user {} ({} active sessions)", userAlerts.size(), userId, sessions.size());
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/alerts", userAlerts);
            return true;
        } catch (Exception e) {
            log.error("Exception while sending alerts to user {}", userId, e);
            return false;
        }
    }

    private void buildDeliveryStatus(List<AlertDeliveryStatus> deliveryResults, List<TriggeredAlert> userAlerts, boolean delivered) {

        AlertChannelStatus status = delivered ? AlertChannelStatus.DELIVERED : AlertChannelStatus.FAILED;
        LocalDateTime now = LocalDateTime.now();

        userAlerts.forEach(alert -> deliveryResults.add(
                new AlertDeliveryStatus(alert.alertId(), AlertChannel.WEBSOCKET, status, now, now)));
    }

    private void recordDeliveryResults(List<TriggeredAlert> triggeredAlerts, List<AlertDeliveryStatus> deliveryResults) {
        try {
            alertService.setAlertAsTriggered(deliveryResults);
            alertService.cleanupTriggeredAlerts(triggeredAlerts);
            alertDeliveryService.recordAlertDeliveries(deliveryResults);
        } catch (Exception e) {
            log.error("Error while recording WebSocket delivery results", e);
        }
    }
}
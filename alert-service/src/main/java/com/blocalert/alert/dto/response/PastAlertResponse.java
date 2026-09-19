package com.blocalert.alert.dto.response;

import com.blocalert.alert.entity.Alert;
import com.blocalert.alert.entity.AlertDelivery;
import com.blocalert.dto.CryptoPrice;
import com.blocalert.enums.AlertChannel;
import com.blocalert.enums.AlertChannelStatus;
import com.blocalert.enums.AlertCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PastAlertResponse {

    private Long id;
    private String cryptoName;
    private String cryptoImage;
    private String cryptoSymbol;
    private AlertCondition condition;
    private BigDecimal thresholdValue;
    private LocalDateTime createdAt;
    private boolean isTriggered;
    private LocalDateTime triggeredAt;
    private boolean notificationWebsocket;
    private boolean notificationEmail;
    private boolean notificationSms;
    private boolean websocketSent;
    private boolean smsSent;
    private boolean emailSent;

    public static PastAlertResponse from(Alert alert, CryptoPrice crypto) {

        List<AlertDelivery> deliveries = alert.getAlertDeliveries();
        return new PastAlertResponse(
                alert.getId(),
                crypto.name(),
                crypto.image(),
                crypto.symbol(),
                alert.getCondition(),
                alert.getThresholdValue(),
                alert.getCreatedAt(),
                alert.getIsTriggered(),
                alert.getTriggeredAt(),
                alert.getNotificationWebsocket(),
                alert.getNotificationEmail(),
                alert.getNotificationSms(),
                isDelivered(deliveries, AlertChannel.WEBSOCKET),
                isDelivered(deliveries, AlertChannel.SMS),
                isDelivered(deliveries, AlertChannel.EMAIL)
        );
    }

    private static boolean isDelivered(List<AlertDelivery> deliveries, AlertChannel alertChannel) {
        return deliveries != null && deliveries.stream().anyMatch(delivery -> delivery.getAlertChannel() == alertChannel && delivery.getAlertStatus() == AlertChannelStatus.DELIVERED);
    }

}


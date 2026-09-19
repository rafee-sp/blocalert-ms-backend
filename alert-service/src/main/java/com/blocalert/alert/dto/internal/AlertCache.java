package com.blocalert.alert.dto.internal;

import com.blocalert.enums.AlertCondition;
import com.blocalert.alert.entity.Alert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertCache {

    private Long alertId;
    private Long userId;
    private String cryptoId;
    private BigDecimal thresholdValue;
    private AlertCondition alertCondition;
    private boolean notificationWebsocket;
    private boolean notificationEmail;
    private boolean notificationSms;

    public static AlertCache from(Alert alert, Long userId) {
        return new AlertCache(
                alert.getId(),
                userId,
                alert.getCryptoId(),
                alert.getThresholdValue(),
                alert.getCondition(),
                alert.getNotificationWebsocket(),
                alert.getNotificationEmail(),
                alert.getNotificationSms()
        );
    }

}

package com.blocalert.dto;

import com.blocalert.enums.AlertChannel;
import com.blocalert.enums.AlertChannelStatus;

import java.time.LocalDateTime;

public record AlertDeliveryStatus(
        Long alertId,
        AlertChannel channel,
        AlertChannelStatus status,
        LocalDateTime triggeredAt,
        LocalDateTime deliveredAt
) {

    public static AlertDeliveryStatus map(Long alertId, AlertChannel channel, AlertChannelStatus status, LocalDateTime triggeredAt, LocalDateTime deliveredAt) {
        return new AlertDeliveryStatus(alertId, channel, status, triggeredAt, deliveredAt);
    }
}

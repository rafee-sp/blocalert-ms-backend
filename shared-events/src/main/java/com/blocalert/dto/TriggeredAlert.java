package com.blocalert.dto;

import com.blocalert.enums.AlertCondition;

import java.math.BigDecimal;

public record TriggeredAlert(
        Long alertId,
        Long userId,
        String cryptoId,
        String cryptoName,
        String cryptoImage,
        BigDecimal thresholdValue,
        AlertCondition alertCondition,
        BigDecimal currentPrice,
        boolean isWebsocketSubscribed,
        boolean isSmsSubscribed,
        boolean isEmailSubscribed
) {
}

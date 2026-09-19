package com.blocalert.dto;

import com.blocalert.enums.AlertCondition;
import java.math.BigDecimal;

public record AssistantAlertRequest (

    String cryptoId,
    Long userId,
    AlertCondition condition,
    BigDecimal thresholdValue,
    Boolean notificationEmail,
    Boolean notificationSms
){}

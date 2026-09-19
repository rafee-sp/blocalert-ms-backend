package com.blocalert.user.domain.assistant.dto;

import com.blocalert.enums.AlertCondition;
import java.math.BigDecimal;

public record AlertCreationResult(
        boolean success,
        String message
) {
    public static AlertCreationResult success(String coinName, AlertCondition condition,
                                              BigDecimal threshold, boolean email, boolean sms, Long alertId) {
        String directionText = switch (condition) {
            case PRICE_ABOVE -> "goes above";
            case PRICE_BELOW -> "goes below";
            case PRICE_EQUALS -> "reaches";
        };
        String channelText = (email && sms) ? "email and SMS"
                : email ? "email"
                : sms ? "SMS"
                : "in-app";

        return new AlertCreationResult(true,
                "Done. I've created an alert to notify you when " + coinName + " " +
                        directionText + " " + threshold + " via " + channelText + ".");
    }

    public static AlertCreationResult failed(String reason) {
        return new AlertCreationResult(false, reason);
    }
}

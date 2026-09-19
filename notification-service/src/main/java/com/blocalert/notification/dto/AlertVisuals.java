package com.blocalert.notification.dto;

import com.blocalert.enums.AlertCondition;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlertVisuals {

    private String emoji;
    private String color;
    private String text;

    public static AlertVisuals getVisual(AlertCondition condition) {
        return switch (condition) {
            case PRICE_ABOVE -> new AlertVisuals("📈", "#16a34a", "rose above");
            case PRICE_BELOW -> new AlertVisuals("📉", "#dc2626", "fell below");
            case PRICE_EQUALS -> new AlertVisuals("🎯", "#2563eb", "reached");
        };
    }
}

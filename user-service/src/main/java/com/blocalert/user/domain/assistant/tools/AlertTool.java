package com.blocalert.user.domain.assistant.tools;

import com.blocalert.dto.CryptoDetails;
import com.blocalert.dto.AssistantAlertRequest;
import com.blocalert.enums.AlertCondition;
import com.blocalert.user.domain.assistant.dto.AlertCreationResult;
import com.blocalert.user.domain.assistant.dto.CryptoDetailResult;
import com.blocalert.user.domain.assistant.service.AlertService;
import com.blocalert.user.domain.assistant.service.CryptoService;
import com.blocalert.user.service.impl.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AlertTool {

    private final CryptoService cryptoService;
    private final AlertService alertService;
    private final CurrentUserProvider userProvider;

    @Tool(description = ChatPrompt.ALERT_CREATION_PROMPT)
    public AlertCreationResult createAlert(
            @ToolParam(description =  ChatPrompt.COIN_QUERY_PROMPT)
            String coinQuery,
            @ToolParam(description =  ChatPrompt.ALERT_CONDITION_PROMPT)
            String condition,
            @ToolParam(description =  ChatPrompt.THRESHOLD_VALUE_PROMPT)
            BigDecimal thresholdValue,
            @ToolParam(description = ChatPrompt.ALERT_EMAIL_PROMPT)
            Boolean notificationEmail,
            @ToolParam(description = ChatPrompt.ALERT_SMS_PROMPT)
            Boolean notificationSms
    ) {

        Long userId = userProvider.getCurrentUserId();

        CryptoDetailResult cryptoResult = cryptoService.getCryptoDetails(coinQuery);

        if(!cryptoResult.available())
            return AlertCreationResult.failed("I couldn't look up \"" + coinQuery + "\" right now. Please try again.");

        if (!cryptoResult.found())
            return AlertCreationResult.failed("I couldn't find a coin matching \"" + coinQuery + "\". Please check the name and try again.");

        CryptoDetails crypto = cryptoResult.cryptoDetails();

        if (thresholdValue == null || thresholdValue.compareTo(BigDecimal.ZERO) <= 0)
            return AlertCreationResult.failed("The price threshold has to be a positive number.");

        AlertCondition alertCondition = getAlertCondition(condition);

        if(alertCondition == null)
            return AlertCreationResult.failed("Condition must be above, below, or equals.");

        boolean isEmailEnabled = Boolean.TRUE.equals(notificationEmail);
        boolean isSmsEnabled = Boolean.TRUE.equals(notificationSms);

        AssistantAlertRequest request = new AssistantAlertRequest(
                crypto.id(),
                userId,
                alertCondition,
                thresholdValue,
                isEmailEnabled,
                isSmsEnabled
        );

        Long alertId = alertService.createAlert(request);

        if(alertId == null)
            return AlertCreationResult.failed("Sorry, I couldn't create the alert. Please try again or create it manually.");

        return AlertCreationResult.success(
                crypto.name(), alertCondition, thresholdValue, isEmailEnabled, isSmsEnabled,alertId
        );
    }

    private AlertCondition getAlertCondition(String alertCondition) {

        if(alertCondition == null) return null;
        String condition = alertCondition.trim().toUpperCase();
        return switch (condition) {
            case "ABOVE" -> AlertCondition.PRICE_ABOVE;
            case "BELOW" -> AlertCondition.PRICE_BELOW;
            case "EQUALS" -> AlertCondition.PRICE_EQUALS;
            default -> null;
        };
    }

}

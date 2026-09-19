package com.blocalert.alert.dto.response;

import com.blocalert.alert.entity.Alert;
import com.blocalert.dto.CryptoPrice;
import com.blocalert.enums.AlertCondition;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ActiveAlertResponse {

    private Long id;
    private String cryptoId;
    private String cryptoSymbol;
    private String cryptoName;
    private String cryptoImage;
    private AlertCondition condition;
    private BigDecimal thresholdValue;
    private boolean notificationWebsocket;
    private boolean notificationEmail;
    private boolean notificationSms;
    private LocalDateTime createdAt;



    public ActiveAlertResponse(Alert alert, CryptoPrice crypto) {

        this.id = alert.getId();
        this.cryptoId = alert.getCryptoId();
        this.cryptoSymbol = crypto.symbol();
        this.cryptoName = crypto.name();
        this.cryptoImage = crypto.image();
        this.condition = alert.getCondition();
        this.thresholdValue = alert.getThresholdValue();
        this.notificationWebsocket = alert.getNotificationWebsocket();
        this.notificationEmail = alert.getNotificationEmail();
        this.notificationSms = alert.getNotificationSms();
        this.createdAt = alert.getCreatedAt();

    }

}

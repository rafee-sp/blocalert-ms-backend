package com.blocalert.alert.service;

import com.blocalert.alert.dto.request.AlertRequest;
import com.blocalert.alert.dto.response.ActiveAlertResponse;
import com.blocalert.alert.dto.response.PastAlertResponse;
import com.blocalert.alert.entity.Alert;
import com.blocalert.dto.AlertDeliveryStatus;
import com.blocalert.dto.TriggeredAlert;
import com.blocalert.dto.AssistantAlertRequest;
import com.blocalert.event.CryptoPriceEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AlertService {

    void evaluateAndPublishAlerts(CryptoPriceEvent event);

    void addAlert(AlertRequest alertRequest);

    void updateAlert(Long alertId, AlertRequest request);

    void deleteAlert(Long id);

    Page<ActiveAlertResponse> getActiveAlerts(String cryptoId, Pageable pageable);

    Page<PastAlertResponse> getPastAlerts(String cryptoId, Pageable pageable);

    List<Alert> getAlertsByIds(List<Long> alertIds);

    void setAlertAsTriggered(List<AlertDeliveryStatus> deliveryResults);

    void cleanupTriggeredAlerts(List<TriggeredAlert> alerts);

    Long createAlertFromAssistant(AssistantAlertRequest request);
}

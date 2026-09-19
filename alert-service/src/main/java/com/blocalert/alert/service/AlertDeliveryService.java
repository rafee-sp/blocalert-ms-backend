package com.blocalert.alert.service;

import com.blocalert.dto.AlertDeliveryStatus;
import java.util.List;

public interface AlertDeliveryService {

    void recordAlertDeliveries(List<AlertDeliveryStatus> alertDeliveryResultList);

    void upsertDelivery(AlertDeliveryStatus alertDeliveryStatus);
}

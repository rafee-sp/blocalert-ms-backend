package com.blocalert.event;


import com.blocalert.dto.AlertDeliveryStatus;
import java.util.List;

public record AlertDeliveryStatusBatchEvent(
        List<AlertDeliveryStatus> alertDeliveryStatusList
) {
}

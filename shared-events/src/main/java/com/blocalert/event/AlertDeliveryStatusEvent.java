package com.blocalert.event;


import com.blocalert.dto.AlertDeliveryStatus;

public record AlertDeliveryStatusEvent(
        AlertDeliveryStatus alertDeliveryStatus
) {
}

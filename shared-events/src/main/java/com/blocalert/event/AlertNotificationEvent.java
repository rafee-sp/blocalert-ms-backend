package com.blocalert.event;

import com.blocalert.dto.TriggeredAlert;

import java.util.List;

public record AlertNotificationEvent(
        List<TriggeredAlert> alertList
) {
}

package com.blocalert.event;

import com.blocalert.dto.SubscriptionNotification;

public record SubscriptionNotificationEvent(
    SubscriptionNotification subscriptionNotification,
    String templateName
){
}

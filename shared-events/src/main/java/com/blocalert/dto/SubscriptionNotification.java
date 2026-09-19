package com.blocalert.dto;

import java.time.LocalDateTime;

public record SubscriptionNotification(
        Long userId,
        String userName,
        String userEmail,
        Long subscriptionId,
        long amount,
        LocalDateTime subscriptionStartDate,
        LocalDateTime subscriptionEndDate,
        String invoiceId,
        String invoiceUrl
) {
}

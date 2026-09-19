package com.blocalert.user.dto.response;

import com.blocalert.user.entity.enums.SubscriptionStatus;
import java.time.LocalDateTime;

public record SubscriptionDetailResponse(
        Long id,
        SubscriptionStatus subscriptionStatus,
        LocalDateTime currentSubscriptionEnd
) {
}

package com.blocalert.user.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class SubscriptionResponse {

    boolean isPaymentCheckedOut;
    boolean isSessionValid;
    boolean isSubscriptionSuccess;
    double amount;
    LocalDate subscriptionStart;
    LocalDate subscriptionEnd;
    String customerEmail;

}

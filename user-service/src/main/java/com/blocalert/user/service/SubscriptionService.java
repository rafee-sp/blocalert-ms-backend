package com.blocalert.user.service;

import com.blocalert.user.dto.response.SubscriptionDetailResponse;
import com.blocalert.user.dto.response.SubscriptionResponse;
import com.blocalert.user.entity.Subscription;
import com.blocalert.user.entity.User;
import com.stripe.exception.StripeException;

public interface SubscriptionService {

    String createSubscription() throws StripeException;

    SubscriptionResponse getSubscriptionSessionDetails(String sessionId) throws StripeException;

    void cancelSubscription() throws StripeException;

    SubscriptionDetailResponse getUserSubscriptionDetails();

    void publishSubscriptionNotification(User user, Subscription subscription, String templateName);
}

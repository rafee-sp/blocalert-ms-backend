package com.blocalert.user.controller;

import com.blocalert.dto.ApiResponse;
import com.blocalert.user.dto.response.SubscriptionDetailResponse;
import com.blocalert.user.dto.response.SubscriptionResponse;
import com.blocalert.user.service.SubscriptionService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('FREE_USER')")
    public ResponseEntity<ApiResponse> subscribeToPlan() throws StripeException {

        log.debug("subscribeToPlan called");

        String subscriptionUrl = subscriptionService.createSubscription();

        return ResponseEntity.ok().body(new ApiResponse("Subscription Url created", subscriptionUrl));
    }

    @PostMapping("/cancel")
    @PreAuthorize("hasRole('PREMIUM_USER')")
    public ResponseEntity<ApiResponse> cancelSubscription() throws StripeException {

        log.debug("cancelSubscription called");

        subscriptionService.cancelSubscription();

        return ResponseEntity.ok().body(new ApiResponse("Subscription Cancelled", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getSubscriptionDetails() {

        log.debug("getSubscriptionDetails called");

        SubscriptionDetailResponse subscriptionDetails = subscriptionService.getUserSubscriptionDetails();
        return ResponseEntity.ok().body(new ApiResponse("Subscriptions fetched", subscriptionDetails));
    }

    @PostMapping("/session-verify")
    public ResponseEntity<ApiResponse> verifySession(@RequestBody Map<String,String> sessionMap) throws StripeException {

        log.debug("verifySession called : {}", sessionMap);

        SubscriptionResponse response = subscriptionService.getSubscriptionSessionDetails(sessionMap.get("sessionId"));
        return ResponseEntity.ok().body(new ApiResponse("Session status fetched", response));
    }

}

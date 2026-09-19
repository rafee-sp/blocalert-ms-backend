package com.blocalert.user.service.impl;

import com.blocalert.dto.SubscriptionNotification;
import com.blocalert.event.SubscriptionNotificationEvent;
import com.blocalert.user.config.StripeConfig;
import com.blocalert.user.dto.response.SubscriptionDetailResponse;
import com.blocalert.user.dto.response.SubscriptionResponse;
import com.blocalert.user.entity.Subscription;
import com.blocalert.user.entity.User;
import com.blocalert.user.entity.enums.SubscriptionStatus;
import com.blocalert.user.exception.ResourceNotFoundException;
import com.blocalert.user.repository.SubscriptionRepository;
import com.blocalert.user.service.SubscriptionService;
import com.blocalert.user.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import static com.blocalert.user.utils.Utils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserService userService;
    private final SubscriptionRepository subscriptionRepository;
    private final StripeConfig stripeConfig;
    private final ApplicationEventPublisher paymentEventPublisher;
    private final CurrentUserProvider userProvider;

    @Override
    public String createSubscription() throws StripeException {

        Long userId = getUserId();

        log.info("createSubscription called {}", userId);

        User user = userService.getUser(userId);

        String stripeCustomerId = getOrCreateCustomerId(user);

        log.debug("stripeCustomerId : {}", stripeCustomerId);
        String correlationId = UUID.randomUUID().toString();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(stripeCustomerId)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(stripeConfig.getPriceId())
                                .setQuantity(1L)
                                .build()
                )
                .setSuccessUrl(stripeConfig.getSuccessUrl())
                .setCancelUrl(stripeConfig.getCancelUrl())
                .setExpiresAt(Instant.now().plus(31L, ChronoUnit.MINUTES).getEpochSecond())
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .putMetadata("userId", user.getId().toString())
                .putMetadata("keycloakId", user.getKeycloakId())
                .putMetadata("correlationId", correlationId)
                .setSubscriptionData(
                        SessionCreateParams.SubscriptionData.builder()
                                .putMetadata("correlationId", correlationId)
                                .build()
                )
                .build();

        Session session = Session.create(params);

        createSubscriptionRecord(user, session.getId(), correlationId);

        log.debug("sessionUrl : {}", session.getUrl());

        return session.getUrl();
    }

    @Override
    public SubscriptionResponse getSubscriptionSessionDetails(String sessionId) throws StripeException {

        Long userId = getUserId();

        log.info("getSubscriptionSessionDetails called for {} - {}", sessionId, userId);

        Session session = Session.retrieve(sessionId);

        User user = userService.getUser(userId);

        if (session == null || session.getSubscription() == null) {
            log.error("No subscription found");
            throw new ResourceNotFoundException("Session not found for Id " + sessionId);
        }

        String paymentStatus = session.getPaymentStatus();

        log.debug("paymentStatus : {}", paymentStatus);

        if (!"paid".equals(paymentStatus)) {
            return SubscriptionResponse.builder().isPaymentCheckedOut(false)
                    .isSessionValid(true)
                    .isSubscriptionSuccess(false)
                    .build();
        }

        Customer customer = Customer.retrieve(session.getCustomer());

        if (customer == null || !customer.getId().equals(user.getStripeCustomerId())) {
            log.debug("No customer found");
            return SubscriptionResponse.builder().isPaymentCheckedOut(true)
                    .isSessionValid(false)
                    .isSubscriptionSuccess(false)
                    .build();
        }

        if (!user.isSubscribed()) {

            log.debug("User's subscription is waiting to be activated {}", userId);

            return SubscriptionResponse.builder().isPaymentCheckedOut(true)
                    .isSessionValid(true)
                    .isSubscriptionSuccess(false)
                    .build();
        }

        return buildSubscriptionResponse(user, sessionId);
    }

    @Override
    @Transactional
    public void cancelSubscription() throws StripeException {

        Long userId = getUserId();
        log.info("cancelSubscription called {}", userId);

        User user = userService.getUser(userId);

        Subscription subscription = subscriptionRepository.findByUser_IdAndSubscriptionStatus(user.getId(), SubscriptionStatus.ACTIVE)
                                                    .orElseThrow(() -> new ResourceNotFoundException("No active Subscription found for user "+ userId));

        com.stripe.model.Subscription stripeSub = com.stripe.model.Subscription.retrieve(subscription.getSubscriptionId());

        SubscriptionUpdateParams updateParams = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(true)
                .build();
        stripeSub.update(updateParams);

        subscription.setCanceledAt(LocalDateTime.now());
        subscription.setIsCancelled(true);
        subscription.setSubscriptionStatus(SubscriptionStatus.CANCELLING);
        subscriptionRepository.save(subscription);

        publishSubscriptionNotification(user, subscription,"SUBSCRIPTION_CANCEL");

        log.info("Subscription set to cancelled");
    }

    @Override
    public SubscriptionDetailResponse getUserSubscriptionDetails() {

        Long userId = getUserId();

        log.info("getUserSubscriptionDetails called {}", userId);

        return subscriptionRepository.getRecentUserSubscription(userId)
                .orElseGet(() -> new SubscriptionDetailResponse(null, SubscriptionStatus.INACTIVE, null));
    }

    private Long getUserId() {
        return userProvider.getCurrentUserId();
    }

    public void publishSubscriptionNotification(User user, Subscription subscription, String templateName) {
        paymentEventPublisher.publishEvent(
                new SubscriptionNotificationEvent(
                        new SubscriptionNotification(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                subscription.getId(),
                                subscription.getAmount(),
                                subscription.getCurrentSubscriptionStart(),
                                subscription.getCurrentSubscriptionEnd(),
                                subscription.getInvoiceId(),
                                subscription.getInvoiceUrl()
                        ),
                        templateName
                )
        );
    }


    private String getOrCreateCustomerId(User user) throws StripeException {

        if (StringUtils.hasText(user.getStripeCustomerId())) return user.getStripeCustomerId();

        CustomerCreateParams params = CustomerCreateParams.builder()
                .setName(user.getName())
                .setEmail(user.getEmail())
                .putMetadata("userId", user.getId().toString())
                .putMetadata("keycloakId", user.getKeycloakId())
                .build();

        String customerId = Customer.create(params).getId();

        userService.updateCustomerId(user.getId(), customerId);

        return customerId;
    }

    private void createSubscriptionRecord(User user, String sessionId, String correlationId) {

        Subscription subscription = Subscription.builder()
                .sessionId(sessionId)
                .user(user)
                .subscriptionStatus(SubscriptionStatus.PENDING)
                .correlationId(correlationId)
                .build();

        subscriptionRepository.save(subscription);
    }

    private SubscriptionResponse buildSubscriptionResponse(User user, String sessionId) {

        Subscription subscription = subscriptionRepository.findByUser_IdAndSessionId(user.getId(), sessionId).orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        double amount = subscription.getAmount() / 100.00;
        LocalDateTime subscriptionStart = subscription.getCurrentSubscriptionStart();
        LocalDateTime subscriptionEnd = subscription.getCurrentSubscriptionEnd();
        String maskedUserEmail = maskEmail(user.getEmail());

        return SubscriptionResponse.builder().isPaymentCheckedOut(true)
                .isSessionValid(true)
                .isSubscriptionSuccess(true)
                .amount(amount)
                .subscriptionStart(subscriptionStart.toLocalDate())
                .subscriptionEnd(subscriptionEnd.toLocalDate())
                .customerEmail(maskedUserEmail)
                .build();
    }
}

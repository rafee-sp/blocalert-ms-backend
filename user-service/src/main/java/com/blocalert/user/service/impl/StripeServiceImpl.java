package com.blocalert.user.service.impl;

import com.blocalert.user.entity.Subscription;
import com.blocalert.user.entity.User;
import com.blocalert.user.entity.WebhookEvent;
import com.blocalert.user.entity.enums.SubscriptionStatus;
import com.blocalert.user.exception.ResourceNotFoundException;
import com.blocalert.user.repository.SubscriptionRepository;
import com.blocalert.user.repository.WebhookEventRepository;
import com.blocalert.user.service.StripeService;
import com.blocalert.user.service.SubscriptionService;
import com.blocalert.user.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceLineItem;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeServiceImpl implements StripeService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final WebhookEventRepository webhookEventRepository;

    private static final String REASON_SUBSCRIPTION_CREATE = "subscription_create";
    private static final String REASON_SUBSCRIPTION_CYCLE = "subscription_cycle";
    private static final String REASON_UPCOMING = "upcoming";

    @Override
    @Transactional
    public void handleStripeCallback(Event event) throws StripeException {

        log.info("handleStripeCallback called for event : {}", event.getType());

        if (existsByEventId(event.getId())) {
            log.warn("handleCheckoutCompleted - Stripe sends duplicate event  : {}", event.getId());
            return;
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutCompleted(event);
            case "invoice.paid" -> handleInvoicePaid(event);
            case "invoice.payment_failed" -> handleInvoicePaymentFailed(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            case "invoice.upcoming" -> handleInvoiceUpcoming(event);
            default -> {
                log.warn("Unhandled event type : {}", event.getType());
                recordEvent(event.getId(), event.getType());
            }
        }
    }

    private void handleCheckoutCompleted(Event event) {

        log.info("handleCheckoutCompleted called");

        Session session = deserializeEvent(event, Session.class);
        String sessionId = session.getId();
        log.debug("sessionId : {}", sessionId);

        Map<String, String> metadata = session.getMetadata();
        String userId = metadata.get("userId");
        String correlationId = metadata.get("correlationId");
        log.debug("userId : {}", userId);

        if (!StringUtils.hasText(userId))
            throw new IllegalStateException("userId metadata missing for checkout session " + sessionId);

        String subscriptionId = session.getSubscription();

        Subscription subscription = subscriptionRepository.findByCorrelationId(correlationId)
                .orElseThrow(() -> new ResourceNotFoundException("subscription not found"));
        subscription.setSubscriptionId(subscriptionId);

        if (subscription.getSubscriptionStatus() != SubscriptionStatus.ACTIVE) {
            subscription.setSubscriptionStatus(SubscriptionStatus.PROCESSING);
        }

        subscriptionRepository.save(subscription);

        recordEvent(event.getId(), event.getType());
    }

    private void handleInvoicePaid(Event event) throws StripeException {

        log.info("handleInvoicePaid called");

        Invoice invoice = deserializeEvent(event, Invoice.class);
        String billingReason = invoice.getBillingReason();

        String stripeSubscriptionId = invoice.getParent().getSubscriptionDetails().getSubscription();

        com.stripe.model.Subscription stripeSubscription =
                com.stripe.model.Subscription.retrieve(stripeSubscriptionId);

        String correlationId = stripeSubscription.getMetadata().get("correlationId");
        // String correlationId = invoice.getParent().getSubscriptionDetails().getMetadata().get("correlationId");

        Subscription subscription = subscriptionRepository.findByCorrelationId(correlationId)
                .orElseThrow(() -> new ResourceNotFoundException("subscription not found for correlationId " + correlationId));
        User user = userService.getUser(subscription.getUser().getId());

        String emailTemplate;

        subscription.setSubscriptionId(stripeSubscriptionId);
        if (REASON_SUBSCRIPTION_CREATE.equals(billingReason)) {

            BillingPeriod period = extractBillingPeriod(invoice);
            subscription.setActivatedAt(LocalDateTime.now());
            subscription.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
            applyInvoiceDetails(subscription, invoice, period);
            emailTemplate = "SUBSCRIPTION_SUCCESS";

        } else if (REASON_SUBSCRIPTION_CYCLE.equals(billingReason)) {

            BillingPeriod period = extractBillingPeriod(invoice);
            subscription.setLastRenewedAt(LocalDateTime.now());
            subscription.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
            applyInvoiceDetails(subscription, invoice, period);
            emailTemplate = "SUBSCRIPTION_RENEWAL";

        } else {
            log.warn("handleInvoicePaid - Unhandled billing reason : {}", billingReason);
            return;
        }

        subscriptionRepository.save(subscription);

        if (REASON_SUBSCRIPTION_CREATE.equals(billingReason))
            userService.upgradeToPremium(user.getId());

        subscriptionService.publishSubscriptionNotification(user, subscription, emailTemplate);

        recordEvent(event.getId(), event.getType());
    }

    private void handleInvoicePaymentFailed(Event event) {

        log.info("handleInvoicePaymentFailedEvent called");

        Invoice invoice = deserializeEvent(event, Invoice.class);
        String subscriptionId = getSubscriptionId(invoice);
        String billingReason = invoice.getBillingReason();

        if (!REASON_SUBSCRIPTION_CYCLE.equals(billingReason)) {
            log.warn("handleInvoicePaymentFailed - Unhandled billing reason {}", billingReason);
            return;
        }

        SubscriptionDetails details = getSubscriptionDetails(invoice.getCustomer(), subscriptionId);

        log.debug("customerId {}, subscriptionId : {}, billingReason : {}", invoice.getCustomer(), subscriptionId, billingReason);

        markSubscriptionPastDue(details.subscription());

        userService.downgradeUserToFree(details.user().getId());

        // TODO: send payment failed email
        recordEvent(event.getId(), event.getType());
    }

    private void handleInvoiceUpcoming(Event event) {

        log.info("handleInvoiceUpcoming called");

        Invoice invoice = deserializeEvent(event, Invoice.class);
        String subscriptionId = getSubscriptionId(invoice);
        String billingReason = invoice.getBillingReason();

        if (!REASON_UPCOMING.equals(billingReason)) {
            log.warn("handleInvoiceUpcoming - Unhandled billing reason {}", billingReason);
            return;
        }

        BillingPeriod period = extractBillingPeriod(invoice);
        SubscriptionDetails details = getSubscriptionDetails(invoice.getCustomer(), subscriptionId);

        log.debug("customerId {}, subscriptionId : {}, billingReason : {}, amount : {}, periodStart : {}, periodEnd : {}",
                invoice.getCustomer(), subscriptionId, billingReason, invoice.getAmountDue(), period.start(), period.end());

        subscriptionService.publishSubscriptionNotification(
                details.user(),
                details.subscription(),
                "SUBSCRIPTION_RENEWAL_REMINDER"
        );

        recordEvent(event.getId(), event.getType());
    }

    private void handleSubscriptionDeleted(Event event) {

        log.info("handleSubscriptionDeleted called");

        com.stripe.model.Subscription stripeSubscription = deserializeEvent(event, com.stripe.model.Subscription.class);
        String subscriptionId = stripeSubscription.getId();

        SubscriptionDetails details = getSubscriptionDetails(stripeSubscription.getCustomer(), subscriptionId);
        Subscription subscription = details.subscription();
        User user = details.user();

        log.debug("customerId {}, subscriptionId : {}", stripeSubscription.getCustomer(), subscriptionId);

        expireOrCancelSubscription(subscription);

        userService.downgradeUserToFree(user.getId());

        if (!subscription.getIsCancelled()) {

            subscriptionService.publishSubscriptionNotification(
                    user,
                    subscription,
                    "SUBSCRIPTION_END"
            );
        }

        // TODO: send different email for already cancelled subscription

        recordEvent(event.getId(), event.getType());
    }

    private void recordEvent(String eventId, String eventType) {

        log.debug("recordEvent called for eventId {}, eventType {}", eventId, eventType);

        webhookEventRepository.save(
                    WebhookEvent.builder()
                            .eventId(eventId)
                            .eventType(eventType)
                            .isProcessed(true)
                            .build()
            );
    }

    private boolean existsByEventId(String eventId) {

        if(!StringUtils.hasText(eventId)) throw new IllegalArgumentException("Event Id is not valid " + eventId);

        return webhookEventRepository.existsByEventId(eventId);
    }

    private void expireOrCancelSubscription(Subscription subscription) {

        if (subscription.getIsCancelled()) {
            subscription.setSubscriptionStatus(SubscriptionStatus.CANCELLED);
        } else {
            subscription.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
            subscription.setExpiredAt(LocalDateTime.now());
        }
        subscriptionRepository.save(subscription);
    }

    private void markSubscriptionPastDue(Subscription subscription) {

        subscription.setSubscriptionStatus(SubscriptionStatus.PAST_DUE);
        subscriptionRepository.save(subscription);
    }

    private LocalDateTime getDateTimeFromUtc(Long dateTime) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(dateTime),
                ZoneId.systemDefault());
    }

    private Subscription getSubscription(Long userId, String subscriptionId) {
        return subscriptionRepository.findByUser_IdAndSubscriptionId(userId, subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with subscription Id  " + subscriptionId));
    }

    private SubscriptionDetails getSubscriptionDetails(String customerId, String subscriptionId) {
        User user = userService.getUserByStripeCustomerId(customerId);
        Subscription subscription = getSubscription(user.getId(), subscriptionId);
        return new SubscriptionDetails(user, subscription);
    }

    private BillingPeriod extractBillingPeriod(Invoice invoice) {

        List<InvoiceLineItem> lineItems = invoice.getLines().getData();

        if (lineItems == null || lineItems.isEmpty())
            throw new IllegalStateException("Invoice lines data is missing or empty for invoice " + invoice.getId());

        InvoiceLineItem lineItem = lineItems.getFirst();
        return new BillingPeriod(lineItem.getPeriod().getStart(), lineItem.getPeriod().getEnd());
    }

    private void applyInvoiceDetails(Subscription subscription, Invoice invoice, BillingPeriod period) {
        subscription.setInvoiceId(invoice.getId());
        subscription.setInvoiceUrl(invoice.getInvoicePdf());
        subscription.setAmount(invoice.getAmountDue());
        subscription.setCurrentSubscriptionStart(getDateTimeFromUtc(period.start()));
        subscription.setCurrentSubscriptionEnd(getDateTimeFromUtc(period.end()));
    }

    private String getSubscriptionId(Invoice invoice) {

        if (invoice.getParent() != null && invoice.getParent().getSubscriptionDetails() != null) {

            String subscriptionId = invoice.getParent()
                    .getSubscriptionDetails()
                    .getSubscription();

            if (StringUtils.hasText(subscriptionId))
                return subscriptionId;
        }

        throw new IllegalStateException("Subscription ID not found for invoice " + invoice.getId());
    }

    private <T extends StripeObject> T deserializeEvent(Event event, Class<T> clazz) {
        return event.getDataObjectDeserializer()
                .getObject()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .orElseThrow(() -> new IllegalStateException("Unable to deserialize Stripe event: " + event.getId()));
    }

    private record SubscriptionDetails(User user, Subscription subscription) {};

    private record BillingPeriod(long start, long end) {};
}

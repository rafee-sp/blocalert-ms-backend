package com.blocalert.notification.service.impl;

import com.blocalert.dto.AlertDeliveryStatus;
import com.blocalert.dto.TriggeredAlert;
import com.blocalert.dto.UserContactInfo;
import com.blocalert.enums.AlertChannel;
import com.blocalert.enums.AlertChannelStatus;
import com.blocalert.event.AlertDeliveryStatusEvent;
import com.blocalert.event.AlertNotificationEvent;
import com.blocalert.notification.config.SmsConfig;
import com.blocalert.notification.dto.AlertVisuals;
import com.blocalert.notification.entity.MessageTemplate;
import com.blocalert.notification.entity.SmsLog;
import com.blocalert.notification.ratelimit.DistributedRateLimiter;
import com.blocalert.notification.service.MessageTemplateService;
import com.blocalert.notification.service.SmsLogService;
import com.blocalert.notification.service.SmsService;
import com.blocalert.notification.utils.Utils;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.security.RequestValidator;
import com.twilio.type.PhoneNumber;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl implements SmsService {

    private final MessageTemplateService messageTemplateService;
    private final SmsLogService smsLogService;
    private final SmsConfig smsConfig;
    private final DistributedRateLimiter rateLimiter;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void sendSmsAlerts(AlertNotificationEvent event, Map<Long, UserContactInfo> usersContactMap) {

        log.info("sendSmsAlerts called for {}", event.alertList().size());

        try {
            List<TriggeredAlert> alerts = event.alertList().stream()
                    .filter(TriggeredAlert::isSmsSubscribed)
                    .toList();

            if (alerts.isEmpty()) return;

            MessageTemplate messageTemplate = messageTemplateService.getTemplate(AlertChannel.SMS, "SMS_ALERT");
            String callbackUrl = smsConfig.getCallbackUrl();

            String templateContent = messageTemplate.getContent();

            List<SmsLog> smsLogs = new ArrayList<>();

            for (TriggeredAlert alert : alerts) {

                String message = null, messageUUID = null;
                AlertChannelStatus status;

                try {

                    UserContactInfo userContactInfo = usersContactMap.get(alert.userId());

                    if (userContactInfo == null || !userContactInfo.isSubscribed() || !StringUtils.hasText(userContactInfo.phoneNumber())) {
                        status = AlertChannelStatus.SKIPPED;
                        log.debug("userContactInfo {}", userContactInfo);
                        log.warn("Skipping sms alert for user {} , alertId={} - invalid or missing contact info", alert.userId(), alert.alertId());

                    } else {

                        message = buildMessage(templateContent, alert);

                        messageUUID = sendSms(
                                alert.alertId(),
                                Utils.formatPhoneNumber(userContactInfo.phoneNumber(), smsConfig.getRegion()),
                                message,
                                callbackUrl + "?alertId=" + alert.alertId()
                        );

                        status = AlertChannelStatus.PENDING;
                    }
                } catch (Exception e) {
                    log.error("Failed to send sms alert for user {} , alertId={}", alert.userId(), alert.alertId(), e);
                    status = AlertChannelStatus.FAILED;
                }

                eventPublisher.publishEvent(mapToEvent(alert.alertId(), status));
                smsLogs.add(buildSmsLog(alert.userId(), alert.alertId(), message, messageUUID));
            }

            smsLogService.saveLogs(smsLogs);
        } catch (
                Exception e) {
            log.error("Error occurred while sending Sms Alerts", e);
        }
    }

    private AlertDeliveryStatusEvent mapToEvent(Long alertId, AlertChannelStatus status) {
        return new AlertDeliveryStatusEvent(
                AlertDeliveryStatus.map(alertId, AlertChannel.SMS, status, LocalDateTime.now(), null));
    }

    private String buildMessage(String templateContent, TriggeredAlert alert) {

        AlertVisuals alertVisuals = AlertVisuals.getVisual(alert.alertCondition());

        return templateContent
                .replace("${emoji}", alertVisuals.getEmoji())
                .replace("${alertConditionText}", alertVisuals.getText())
                .replace("${cryptoName}", alert.cryptoName())
                .replace("${thresholdValue}", Utils.formatPrice(alert.thresholdValue()))
                .replace("${currentPrice}", Utils.formatPrice(alert.currentPrice()))
                .replace("${timestamp}", Utils.getFormattedCurrDate());
    }

    @Override
    public boolean isValidTwilioResponse(Map<String, String> paramsMap, String signature, HttpServletRequest request) {

        log.info("isValidTwilioResponse called");

        try {

            if (signature == null || signature.isEmpty()) {
                log.warn("Empty signature");
                return false;
            }

            String scheme = Optional.ofNullable(request.getHeader("X-Forwarded-Proto"))
                    .orElse(request.getScheme());

            String host = Optional.ofNullable(request.getHeader("X-Forwarded-Host"))
                    .orElse(request.getServerName());

            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(scheme).append("://").append(host).append(request.getRequestURI());

            if (request.getQueryString() != null)
                urlBuilder.append("?").append(request.getQueryString());

            String fullUrl = urlBuilder.toString();

            RequestValidator validator = new RequestValidator(smsConfig.getAuthId());

            return validator.validate(fullUrl, paramsMap, signature);

        } catch (Exception ex) {
            log.error("Error validating Twilio/Plivo signature", ex);
            return false;
        }

    }

    @Override
    public void handleSmsMessageCallback(String alertIdStr, Map<String, String> paramsMap) {

        log.debug("handleSmsMessageCallback called {}", alertIdStr);

        if (!StringUtils.hasText(alertIdStr)) return;

        Long alertId = Long.valueOf(alertIdStr);

        if (paramsMap == null || paramsMap.isEmpty()) {
            log.error("Twilio sends response with missing parameters");
            return;
        }

        String messageSID = paramsMap.get("MessageSid");
        String status = paramsMap.get("MessageStatus");

        log.debug("MessageSid : {} status {}", messageSID, status);

        if (!StringUtils.hasText(messageSID) || !StringUtils.hasText(status)) {
            log.error("No messageId or status present in the Text callback messageId - {} status - {}", messageSID, status);
            return;
        }

        AlertChannelStatus mappedStatus = mapTwilioStatus(status);

        if (mappedStatus == null) {
            log.debug("Ignoring non-terminal Twilio status: {}", status);
            return;
        }

        try {

            eventPublisher.publishEvent(mapToEvent(alertId, mappedStatus));

        } catch (Exception e) {
            log.error("Failed to update message status for MessageSid: {}, status: {}, alertId {}", messageSID, status, alertId, e);
        }
    }

    private AlertChannelStatus mapTwilioStatus(String twilioStatus) {
        return switch (twilioStatus.toLowerCase()) {
            case "delivered" -> AlertChannelStatus.DELIVERED;
            case "failed", "undelivered" -> AlertChannelStatus.FAILED;
            default -> null; // queued, sending, sent — not terminal
        };
    }

    @Retryable(
            includes = {com.twilio.exception.ApiConnectionException.class},
            maxRetries = 3,
            delay = 500,
            multiplier = 2.0
    )
    private String sendSms(Long alertId, String userPhoneNumber, String content, String callbackUrl) throws InterruptedException {

        rateLimiter.consumeSms();

        Message message = Message.creator(new PhoneNumber(userPhoneNumber),
                        new PhoneNumber(smsConfig.getPhone()),
                        content)
                .setStatusCallback(URI.create(callbackUrl))
                .create();

        if (!StringUtils.hasText(message.getSid()))
            throw new RuntimeException("Message not sent for alert " + alertId);

        return message.getSid();
    }

    private SmsLog buildSmsLog(Long userId, Long alertId, String message, String messageUUID) {
        return SmsLog.builder()
                .alertId(alertId)
                .userId(userId)
                .content(message)
                .messageUUId(messageUUID)
                .build();
    }
}

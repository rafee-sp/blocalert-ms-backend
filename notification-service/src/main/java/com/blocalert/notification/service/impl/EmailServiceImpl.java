package com.blocalert.notification.service.impl;

import com.blocalert.dto.AlertDeliveryStatus;
import com.blocalert.dto.SubscriptionNotification;
import com.blocalert.enums.AlertChannel;
import com.blocalert.dto.TriggeredAlert;
import com.blocalert.dto.UserContactInfo;
import com.blocalert.enums.AlertChannelStatus;
import com.blocalert.event.AlertDeliveryStatusBatchEvent;
import com.blocalert.event.AlertNotificationEvent;
import com.blocalert.notification.config.AppConfig;
import com.blocalert.notification.config.MailConfig;
import com.blocalert.notification.dto.AlertVisuals;
import com.blocalert.notification.entity.EmailLog;
import com.blocalert.notification.entity.MessageTemplate;
import com.blocalert.notification.ratelimit.DistributedRateLimiter;
import com.blocalert.notification.service.EmailLogService;
import com.blocalert.notification.service.EmailService;
import com.blocalert.notification.service.MessageTemplateService;
import com.blocalert.notification.utils.Utils;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final MessageTemplateService messageTemplateService;
    private final EmailLogService emailLogService;
    private final JavaMailSender mailSender;
    private final MailConfig mailConfig;
    private final AppConfig appConfig;
    private final DistributedRateLimiter rateLimiter;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void sendEmailAlerts(AlertNotificationEvent event, Map<Long, UserContactInfo> usersContactMap) {

        log.info("sendEmailAlerts called for {}", event.alertList().size());

        try {

            List<TriggeredAlert> alerts = event.alertList().stream()
                    .filter(TriggeredAlert::isEmailSubscribed)
                    .toList();

            if (alerts.isEmpty()) return;

            String frontendUrl = appConfig.getFrontendUrl();

            MessageTemplate messageTemplate = getTemplate("EMAIL_ALERT");
            String defaultSubject = messageTemplate.getSubject();
            String templateContent = messageTemplate.getContent();

            List<AlertDeliveryStatus> deliveryResults = new ArrayList<>();
            List<EmailLog> emailLogs = new ArrayList<>();

            for (TriggeredAlert alert : alerts) {
                String subject, message = "";
                AlertChannelStatus status;
                LocalDateTime triggeredAt = LocalDateTime.now();
                LocalDateTime deliveredAt = null;
                try {

                    UserContactInfo userContactInfo = usersContactMap.get(alert.userId());

                    if (userContactInfo == null || !userContactInfo.isSubscribed() || !StringUtils.hasText(userContactInfo.email())) {
                        status = AlertChannelStatus.SKIPPED;
                        log.debug("userContactInfo {}", userContactInfo);
                        log.warn("Skipping email alert for user {} , alertId={} - invalid or missing contact info", alert.userId(), alert.alertId());

                    } else {
                        message = buildMail(alert, templateContent, frontendUrl);
                        subject = buildSubject(alert, defaultSubject);

                        sendMail(userContactInfo.email(), subject, message);
                        status = AlertChannelStatus.DELIVERED;
                        deliveredAt = LocalDateTime.now();
                    }

                } catch (Exception e) {
                    log.error("Failed to send email alert for user {}, alertId={}", alert.userId(), alert.alertId(), e);
                    status = AlertChannelStatus.FAILED;
                }

                deliveryResults.add(AlertDeliveryStatus.map(alert.alertId(), AlertChannel.EMAIL, status, triggeredAt, deliveredAt));

                if (status == AlertChannelStatus.DELIVERED || status == AlertChannelStatus.FAILED)
                    emailLogs.add(buildEmailLog(alert.userId(), alert.alertId(), message));
            }

            eventPublisher.publishEvent(new AlertDeliveryStatusBatchEvent(deliveryResults));
            emailLogService.saveLogs(emailLogs);

        } catch (Exception e) {
            log.error("Error occurred while sending Email Alerts", e);
        }
    }

    private String buildSubject(TriggeredAlert alert, String subject) {

        AlertVisuals alertVisuals = AlertVisuals.getVisual(alert.alertCondition());

        return subject
                .replace("${emoji}", alertVisuals.getEmoji())
                .replace("${cryptoName}", alert.cryptoName())
                .replace("${alertConditionText}", alertVisuals.getText())
                .replace("${thresholdValue}", Utils.formatPrice(alert.thresholdValue()));
    }

    private String buildMail(TriggeredAlert alert, String template, String frontendUrl) {

        AlertVisuals alertVisuals = AlertVisuals.getVisual(alert.alertCondition());

        return template
                .replace("${accentColor}", alertVisuals.getColor())
                .replace("${emoji}", alertVisuals.getEmoji())
                .replace("${alertConditionText}", alertVisuals.getText())
                .replace("${alertConditionTextButton}", StringUtils.capitalize(alertVisuals.getText()))
                .replace("${cryptoName}", alert.cryptoName())
                .replace("${cryptoImage}", alert.cryptoImage())
                .replace("${thresholdValue}", Utils.formatPrice(alert.thresholdValue()))
                .replace("${currentPrice}", Utils.formatPrice(alert.currentPrice()))
                .replace("${timestamp}", Utils.getFormattedCurrDate())
                .replace("${userId}", String.valueOf(alert.userId()))
                .replace("${dashboardUrl}", frontendUrl);
    }

    @Override
    public void sendSubscriptionMail(SubscriptionNotification subscription, String templateName) {

        try {

            log.info("sendSubscriptionMail called for {} {}", subscription.subscriptionId(), templateName);

            MessageTemplate messageTemplate = getTemplate(templateName);

            String subject = messageTemplate.getSubject();
            String content = messageTemplate.getContent();

            String updatedMessage = buildSubscriptionTemplate(content, subscription);

            sendMail(subscription.userEmail(), subject, updatedMessage);

            emailLogService.saveLog(buildEmailLog(subscription.userId(), null, updatedMessage));
        } catch (Exception e) {
            log.error("Error occurred while sending subscription email", e);
        }
    }

    private String buildSubscriptionTemplate(String content, SubscriptionNotification subscription) {

        String amount =  Utils.formatSubscriptionPrice(subscription.amount());
        String startDate = Utils.getAbbreviatedDateFormat(subscription.subscriptionStartDate());
        String endDate = Utils.getAbbreviatedDateFormat(subscription.subscriptionEndDate());

        return content
                .replace("${userName}", subscription.userName())
                .replace("${planName}", "Premium Plan")  // TODO : Currently only sending mail for Premium user
                .replace("${amount}", amount)
                .replace("${billingPeriod}", "Monthly")
                .replace("${startDate}", startDate)
                .replace("${nextBillingDate}", endDate)
                .replace("${accessUntilDate}", endDate)
                .replace("${renewalDate}", endDate)
                .replace("${endDate}", endDate)
                .replace("${invoiceId}", subscription.invoiceId())
                .replace("${dashboardUrl}", appConfig.getFrontendUrl())
                .replace("${invoiceUrl}", subscription.invoiceUrl())
                .replace("${currentYear}", String.valueOf(Year.now().getValue()));
    }

    @Retryable(
            includes = {jakarta.mail.MessagingException.class, org.springframework.mail.MailSendException.class},
            maxRetries = 3,
            delay = 500,
            multiplier = 2.0
    )
    private void sendMail(String to, String subject, String content) throws Exception {

        rateLimiter.consumeEmail();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailConfig.getFrom(), mailConfig.getName());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(message);
    }

    private MessageTemplate getTemplate(String templateCode) {
        return messageTemplateService.getTemplate(AlertChannel.EMAIL, templateCode);
    }

    private EmailLog buildEmailLog(Long userId, Long alertId, String message) {

        return EmailLog.builder()
                .userId(userId)
                .alertId(alertId)
                .content(message)
                .build();
    }
}
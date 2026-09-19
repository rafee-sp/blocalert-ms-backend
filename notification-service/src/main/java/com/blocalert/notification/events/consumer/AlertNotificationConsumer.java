package com.blocalert.notification.events.consumer;

import com.blocalert.dto.TriggeredAlert;
import com.blocalert.dto.UserContactInfo;
import com.blocalert.event.AlertNotificationEvent;
import com.blocalert.notification.service.EmailService;
import com.blocalert.notification.service.SmsService;
import com.blocalert.notification.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertNotificationConsumer {

    private final EmailService emailService;
    private final SmsService smsService;
    private final UserService userService;
    private final Executor notificationExecutor;

    @Bean
    Consumer<AlertNotificationEvent> alertNotification() {
        return event -> {

            log.info("alertNotification called for {}", event.alertList().size());

            Set<Long> userIds = event.alertList().stream()
                    .map(TriggeredAlert::userId)
                    .collect(Collectors.toSet());

            Map<Long, UserContactInfo> usersContactMap = userService.fetchContactInfo(userIds);

            CompletableFuture<Void> emailFuture = CompletableFuture.runAsync(
                    () -> emailService.sendEmailAlerts(event, usersContactMap), notificationExecutor
            ).exceptionally(ex -> {
                log.error("Email alerts failed ", ex);
                return null;
            });

            CompletableFuture<Void> smsFuture = CompletableFuture.runAsync(
                    () -> smsService.sendSmsAlerts(event, usersContactMap), notificationExecutor
            ).exceptionally(ex -> {
                log.error("Sms alerts failed ", ex);
                return null;
            });

            CompletableFuture.allOf(emailFuture, smsFuture).join();
        };
    }
}

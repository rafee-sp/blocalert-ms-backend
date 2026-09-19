package com.blocalert.notification.events.consumer;

import com.blocalert.event.SubscriptionNotificationEvent;
import com.blocalert.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class SubscriptionNotificationConsumer {

    private final EmailService emailService;

    @Bean
    Consumer<SubscriptionNotificationEvent> subscriptionNotification(){
        return event -> emailService
                .sendSubscriptionMail(event.subscriptionNotification(), event.templateName());
    }
}

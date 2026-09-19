package com.blocalert.notification.service;

import com.blocalert.dto.SubscriptionNotification;
import com.blocalert.dto.UserContactInfo;
import com.blocalert.event.AlertNotificationEvent;
import java.util.Map;

public interface EmailService {

    void sendEmailAlerts(AlertNotificationEvent event, Map<Long, UserContactInfo> usersContactMap);

    void sendSubscriptionMail(SubscriptionNotification subscription, String templateName);

}

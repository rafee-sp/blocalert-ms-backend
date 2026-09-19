package com.blocalert.notification.service;

import com.blocalert.dto.UserContactInfo;
import com.blocalert.event.AlertNotificationEvent;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface SmsService {

    void sendSmsAlerts(AlertNotificationEvent event, Map<Long, UserContactInfo> usersContactMap);

    boolean isValidTwilioResponse(Map<String, String> paramsMap, String signature, HttpServletRequest request);

    void handleSmsMessageCallback(String alertId, Map<String, String> paramsMap);
}

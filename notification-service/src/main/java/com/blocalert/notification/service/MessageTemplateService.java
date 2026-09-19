package com.blocalert.notification.service;

import com.blocalert.enums.AlertChannel;
import com.blocalert.notification.entity.MessageTemplate;

public interface MessageTemplateService {

    MessageTemplate getTemplate(AlertChannel channel, String templateCode);
}

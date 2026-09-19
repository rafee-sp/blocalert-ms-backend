package com.blocalert.notification.service.impl;

import com.blocalert.enums.AlertChannel;
import com.blocalert.notification.entity.MessageTemplate;
import com.blocalert.notification.exception.ResourceNotFoundException;
import com.blocalert.notification.repository.MessageTemplateRepository;
import com.blocalert.notification.service.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageTemplateServiceImpl implements MessageTemplateService {

    private final MessageTemplateRepository messageTemplateRepository;

    @Override
    @Cacheable(value = "messageTemplates", key = "#channel + ':' + #templateCode")
    public MessageTemplate getTemplate(AlertChannel channel, String templateCode) {

        log.debug("MessageTemplate called for {} - {}", channel, templateCode);

        return messageTemplateRepository.findByChannelAndCode(channel, templateCode)
                .orElseThrow(() -> new ResourceNotFoundException("Message Template not found for code: " + templateCode + " and channel: " + channel));
    }
}

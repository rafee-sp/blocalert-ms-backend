package com.blocalert.user.domain.assistant.client;

import com.blocalert.dto.AssistantAlertRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface AlertServiceClient {

    @PostExchange("/api/alerts/assistant")
    Long createAlertFromAssistant(@RequestBody AssistantAlertRequest request);
}

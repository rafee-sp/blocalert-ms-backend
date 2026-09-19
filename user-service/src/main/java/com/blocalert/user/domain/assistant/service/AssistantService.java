package com.blocalert.user.domain.assistant.service;

import com.blocalert.user.domain.assistant.dto.ChatResponse;
import com.blocalert.user.domain.assistant.tools.AlertTool;
import com.blocalert.user.domain.assistant.tools.ChatPrompt;
import com.blocalert.user.domain.assistant.tools.CryptoTool;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AssistantService {

    private final ChatClient chatClient;

    public AssistantService(ChatClient.Builder chatClientBuilder,
                            CryptoTool cryptoTool,
                            AlertTool alertTool) {

        this.chatClient = chatClientBuilder
                .defaultSystem(ChatPrompt.SYSTEM_PROMPT)
                .defaultTools(cryptoTool, alertTool)
                .build();

    }

    @RateLimiter(name = "chatAssistant", fallbackMethod = "rateLimitFallback")
    public ChatResponse handleMessage(String userMessage) {

        String reply = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();

        return ChatResponse.of(reply);
    }

    public ChatResponse rateLimitFallback(String userMessage, Throwable t) {
        log.error("rateLimitFallback called {}", t.getMessage());
        return ChatResponse.of("You're sending messages too quickly, please wait a moment and try again.");
    }
}

package com.blocalert.user.domain.assistant.controller;

import com.blocalert.user.domain.assistant.dto.ChatRequest;
import com.blocalert.user.domain.assistant.dto.ChatResponse;
import com.blocalert.user.domain.assistant.service.AssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('PREMIUM_USER')")
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("AssistantController(chat) called");
        ChatResponse response = assistantService.handleMessage(request.message());
        return ResponseEntity.ok().body(response);
    }

}

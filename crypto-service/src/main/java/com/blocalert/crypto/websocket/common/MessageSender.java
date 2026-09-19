package com.blocalert.crypto.websocket.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSender {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendCryptoHomeMessage(String sessionId, String destination, Object payload) {

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        messagingTemplate.convertAndSendToUser(sessionId, destination, payload, headerAccessor.getMessageHeaders());
    }

    public void sendCryptoDetailMessage(String userId, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/crypto/detail",
                payload
        );
    }

    public void sendTopicMessage(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
    }
}

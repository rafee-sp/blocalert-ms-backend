package com.blocalert.alert.websocket;

import com.blocalert.alert.service.UserService;
import com.blocalert.alert.service.impl.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import java.security.Principal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompInterceptor implements ChannelInterceptor {

    private final AuthService authService;
    private final UserService userService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authorization = accessor.getFirstNativeHeader("Authorization");

            if (authorization == null || !authorization.startsWith("Bearer "))
                throw new MessagingException("Missing auth token");

            String token = authorization.substring(7);

            try {

                Optional<String> keycloakId = authService.validateAndGetKeycloakUserId(token);
                if (keycloakId.isEmpty()) {
                    throw new MessagingException("Invalid auth token");
                }

                Long userId = userService.getUserId(keycloakId.get());
                if (userId == null || userId <= 0) {
                    throw new MessagingException("User not found "+ keycloakId.get());
                }
                log.info("Authenticated STOMP user={}", userId);
                accessor.setUser(new StompPrincipal(String.valueOf(userId)));

            } catch (JwtException e) {
                throw new MessagingException("Invalid auth token");
            }
        }
        return message;
    }

    public record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() {return name;}
    }
}

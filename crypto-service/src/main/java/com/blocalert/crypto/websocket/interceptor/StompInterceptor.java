package com.blocalert.crypto.websocket.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        boolean requiresAuth = accessor.getSessionAttributes() != null
                                    && Boolean.TRUE.equals(accessor.getSessionAttributes().get("requiresAuth"));

        if (!requiresAuth) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authorization = accessor.getFirstNativeHeader("Authorization");

            if (authorization == null || !authorization.startsWith("Bearer "))
                throw new MessagingException("Missing auth token");

            String token = authorization.substring(7);

            try {

                Jwt jwt = jwtDecoder.decode(token);
                String userId = jwt.getSubject();
                log.info("Authenticated STOMP user={}", userId);
                accessor.setUser(new StompPrincipal(userId));

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
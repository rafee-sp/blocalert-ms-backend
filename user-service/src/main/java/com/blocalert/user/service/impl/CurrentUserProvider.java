package com.blocalert.user.service.impl;

import com.blocalert.user.exception.UnauthorizedException;
import com.blocalert.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentUserProvider{

    private final UserService userService;
    private final JwtDecoder jwtDecoder;

    public Long getCurrentUserId() {
        String keycloakId = getCurrentKeycloakId();
        return userService.getUserIdByKeycloakId(keycloakId);
    }

    private String getCurrentKeycloakId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("User is not authenticated");
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String subject = jwt.getSubject();
            if(!StringUtils.hasText(subject))
                throw new UnauthorizedException("JWT subject is missing");
            return subject;
        }
        throw new UnauthorizedException("Invalid authentication type");
    }
}

package com.blocalert.alert.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtDecoder jwtDecoder;

    public Optional<String> validateAndGetKeycloakUserId(String token) {
        try {

            if(!StringUtils.hasText(token)) return Optional.empty();

            Jwt jwt = jwtDecoder.decode(token);

            String sub = jwt.getSubject();

            return StringUtils.hasText(sub) ? Optional.of(sub) : Optional.empty();

        } catch (JwtException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Exception occurred ", e);
            return Optional.empty();
        }
    }

    public String getKeycloakId() {

        Authentication authentication = getAuthentication();

        if(!(authentication.getPrincipal() instanceof Jwt jwt))
            throw new IllegalStateException("No authenticated JWT found");

        String keycloakId = jwt.getSubject();

        if (!StringUtils.hasText(keycloakId))
            throw new IllegalStateException("Authenticated JWT has no subject");

        return keycloakId;
    }

    public boolean hasRole(String role){
        return getAuthentication().getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("No authenticated user");
        }

        return authentication;
    }
}

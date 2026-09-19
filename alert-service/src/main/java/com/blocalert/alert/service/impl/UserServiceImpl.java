package com.blocalert.alert.service.impl;

import com.blocalert.alert.client.UserServiceClient;
import com.blocalert.alert.exception.UserServiceUnavailableException;
import com.blocalert.alert.service.UserService;
import com.blocalert.dto.UserIdResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserServiceClient userServiceClient;
    private final AuthService authService;

    @Override
    public Long getCurrentUserId() {
        return getUserId(authService.getKeycloakId());
    }

    @Override
    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "getUserIdFallback")
    @Retry(name = "userServiceCB")
    public Long getUserId(String keycloakId) {

        UserIdResponse userId = userServiceClient.getUserId(keycloakId);

        return userId.userId();
    }

    private Long getUserIdFallback(String keycloakId, Throwable throwable) {
        log.error("user-service unavailable while resolving local id for keycloakId={}", keycloakId, throwable);
        throw new UserServiceUnavailableException("Unable to resolve user identity right now");
    }
}

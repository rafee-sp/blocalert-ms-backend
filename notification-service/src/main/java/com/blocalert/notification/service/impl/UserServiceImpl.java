package com.blocalert.notification.service.impl;

import com.blocalert.dto.UserContactInfo;
import com.blocalert.notification.client.UserServiceClient;
import com.blocalert.notification.exception.UserServiceUnavailableException;
import com.blocalert.notification.service.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserServiceClient userServiceClient;

    @Override
    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "fetchContactInfoFallback")
    @Retry(name = "userServiceCB")
    public Map<Long, UserContactInfo> fetchContactInfo(Set<Long> userIds) {
        log.info("Fetching user contact info for {} ids", userIds.size());
        return userServiceClient.getContactInfo(userIds);
    }

    private Map<Long, UserContactInfo> fetchContactInfoFallback(Set<Long> userIds, Throwable throwable) {
        log.error("user-service unavailable while fetching contact info for {} ids", userIds.size(), throwable);
        throw new UserServiceUnavailableException("user-service unavailable, cannot resolve delivery contact info");
    }
}

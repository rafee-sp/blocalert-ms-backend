package com.blocalert.alert.client;

import com.blocalert.dto.UserIdResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface UserServiceClient {

    @GetExchange("/api/users/local-id")
    UserIdResponse getUserId(@RequestParam String keycloakId);
}

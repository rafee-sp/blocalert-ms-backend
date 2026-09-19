package com.blocalert.notification.client;

import com.blocalert.dto.UserContactInfo;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Map;
import java.util.Set;

@HttpExchange
public interface UserServiceClient {

    @GetExchange("/api/users/contact-info")
    Map<Long, UserContactInfo> getContactInfo(@RequestParam("ids") Set<Long> userIds);
}

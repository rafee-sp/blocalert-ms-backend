package com.blocalert.crypto.websocket.service;

import com.blocalert.crypto.dto.internal.PageDTO;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HomepageSubscriptionRegistry {

    private final Map<String, PageDTO> subscriptions = new ConcurrentHashMap<>();

    public void set(String userId, PageDTO subscription){
        subscriptions.put(userId, subscription);
    }

    public void remove(String userId){
        subscriptions.remove(userId);
    }

    public Map<String, PageDTO> getAll(){
        return subscriptions;
    }
}

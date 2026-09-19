package com.blocalert.crypto.websocket.service;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CryptoDetailSubscriptionRegistry {

    private final Map<String, Set<String>> subscriptions = new ConcurrentHashMap<>();

    public void add(String userId, String symbol) {
        subscriptions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(symbol);
    }

    public void remove(String userId, String symbol) {
        Set<String> symbols = subscriptions.get(userId);
        if (symbols != null) symbols.remove(symbol);
    }

    public void removeAll(String userId) {
        subscriptions.remove(userId);
    }

    public Map<String, Set<String>> getAll() {
        return subscriptions;
    }
}
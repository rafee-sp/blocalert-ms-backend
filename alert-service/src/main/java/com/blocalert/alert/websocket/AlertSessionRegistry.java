package com.blocalert.alert.websocket;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlertSessionRegistry {

    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    public void addSession(Long userId, String sessionId) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public void removeSession(Long userId, String sessionId){
        Set<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if(sessions.isEmpty()) userSessions.remove(userId, sessions);
        }
    }

    public Set<String> getSessions(Long userId) {
        return userSessions.getOrDefault(userId, Set.of());
    }

    public int userCount() {
        return userSessions.size();
    }

    public void clear() {
        userSessions.clear();
    }
}

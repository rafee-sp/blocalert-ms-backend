package com.blocalert.alert.repository;

import com.blocalert.alert.dto.internal.AlertCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AlertCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(AlertCache alert){
        try {

            redisTemplate.opsForHash().put(
                            alertKey(alert.getCryptoId()),
                            alertField(alert.getAlertId()),
                            alert
                    );

        } catch (Exception e) {
            log.error("Failed to cache alert {}", alert.getAlertId(), e);
        }
    }

    public void delete(Long alertId, String cryptoId){
        try {
                redisTemplate.opsForHash().delete(
                        alertKey(cryptoId),
                        alertField(alertId)
                );
        } catch (Exception e) {
            log.error("Failed to delete cached alert {}", alertId, e);
        }
    }

    public void deleteAll(String cryptoId, Collection<Long> alertIds) {
        try {

            Object[] fields = alertIds.stream()
                    .map(this::alertField)
                    .toArray();

            redisTemplate.opsForHash().delete(
                            alertKey(cryptoId),
                            fields
                    );

        } catch (Exception e) {
            log.error("Failed to delete cached alerts for crypto {}", cryptoId, e);
        }
    }

    public Map<String, List<AlertCache>> findByCryptoIds(Set<String> cryptoIds) {

        Map<String, List<AlertCache>> result = new HashMap<>();

        try {

            List<Object> responses = redisTemplate.executePipelined(
                    (RedisCallback<Object>) connection -> {
                        cryptoIds.forEach(id ->
                                connection.hashCommands().hGetAll(
                                        alertKey(id).getBytes(StandardCharsets.UTF_8)));
                        return null;
                    });

            Iterator<String> iterator = cryptoIds.iterator();

            for (Object response : responses) {

                String cryptoId = iterator.next();

                if (!(response instanceof Map<?, ?> map) || map.isEmpty()) {
                    result.put(cryptoId, Collections.emptyList());
                    continue;
                }

                List<AlertCache> alerts = map.values().stream()
                        .map(AlertCache.class::cast)
                        .toList();

                result.put(cryptoId, alerts);
            }
        } catch (Exception e) {
            log.error("Failed to load cached alerts", e);
        }
        return result;
    }

    private String alertKey(String cryptoId) {
        return "alert:coin:" + cryptoId;
    }

    private String alertField(Long alertId) {
        return "alert:" + alertId;
    }
}

package com.blocalert.crypto.repository;

import com.blocalert.dto.CryptoDetails;
import com.blocalert.crypto.dto.internal.CryptoSummary;
import com.blocalert.crypto.dto.internal.CryptoSummaryCache;
import com.blocalert.crypto.dto.internal.MarketStatsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CryptoCacheRepository {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CRYPTO_FULL_HASH = "crypto:data:full";
    private static final String CRYPTO_DATA_LITE = "crypto:data:lite";
    private static final String MARKET_STATS = "market:stats";

    public void saveCryptoDetails(List<CryptoDetails> cryptoList) {

        Map<String, Object> values = new HashMap<>();

        for (CryptoDetails crypto : cryptoList) {
            values.put(crypto.id(), crypto);
        }

        redisTemplate.opsForHash().putAll(CRYPTO_FULL_HASH, values);
    }

    public void saveCryptoSummary(List<CryptoSummary> cryptoList) {
        redisTemplate.opsForValue().set(CRYPTO_DATA_LITE, new CryptoSummaryCache(cryptoList));
    }

    public void saveMarketStats(MarketStatsData marketStatsData) {
        redisTemplate.opsForValue().set(MARKET_STATS, marketStatsData);
    }

    public List<CryptoDetails> getAllCryptoDetails() {
        return redisTemplate.opsForHash()
                .values(CRYPTO_FULL_HASH)
                .stream()
                .map(v -> (CryptoDetails) v)
                .toList();
    }

    public CryptoDetails findById(String cryptoId) {
        Object value = redisTemplate.opsForHash().get(CRYPTO_FULL_HASH, cryptoId);
        return value != null ? (CryptoDetails) value : null;
    }

    public Map<String, CryptoDetails> findByIds(Set<String> ids) {

        Map<String, CryptoDetails> result = new HashMap<>();

        List<String> idList = new ArrayList<>(ids);
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();

        List<Object> values = hashOperations.multiGet(CRYPTO_FULL_HASH, idList);

        if (values == null) {
            return Collections.emptyMap();
        }

        for (int i = 0; i < idList.size(); i++) {

            Object value = values.get(i);
            if (value != null)
                result.put(idList.get(i), (CryptoDetails) value);
        }

        return result;
    }


    @SuppressWarnings("unchecked")
    public List<CryptoSummary> findCryptoSummaryList() {
        try {
            Object value = redisTemplate.opsForValue().get(CRYPTO_DATA_LITE);
            CryptoSummaryCache cache = value != null ? (CryptoSummaryCache) value : null;
            return cache.cryptoSummaryList();
        } catch (Exception e) {
            log.error("Failed to read lite crypto list from cache", e);
            return null;
        }
    }

    public MarketStatsData findMarketStats() {
        try {
            return (MarketStatsData) redisTemplate.opsForValue().get(MARKET_STATS);
        } catch (Exception e) {
            log.error("Failed to read market stats from cache", e);
            return null;
        }
    }

}

package com.blocalert.notification.ratelimit;

import com.blocalert.notification.config.MailConfig;
import com.blocalert.notification.config.SmsConfig;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RateLimiterConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    private final MailConfig mailConfig;
    private final SmsConfig smsConfig;

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create(
                RedisURI.builder()
                        .withHost(redisHost)
                        .withPort(redisPort)
                        .build()
        );
    }

    @Bean
    public ProxyManager<String> redisProxyManager(RedisClient redisClient) {

        StatefulRedisConnection<String, byte[]> connection =
                redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        return Bucket4jLettuce.casBasedBuilder(connection)
                .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1)))
                .build();
    }

    @Bean("emailBucketConfiguration")
    public BucketConfiguration emailBucketConfiguration() {

        return BucketConfiguration.builder()
                .addLimit(limit -> limit
                        .capacity(mailConfig.getRateLimit())
                        .refillGreedy(mailConfig.getRateLimit(), Duration.ofSeconds(1))
                )
                .build();
    }

    @Bean("smsBucketConfiguration")
    public BucketConfiguration smsBucketConfiguration() {

        return BucketConfiguration.builder()
                .addLimit(limit -> limit
                        .capacity(smsConfig.getRateLimit())
                        .refillGreedy(smsConfig.getRateLimit(), Duration.ofSeconds(1))
                )
                .build();
    }
}

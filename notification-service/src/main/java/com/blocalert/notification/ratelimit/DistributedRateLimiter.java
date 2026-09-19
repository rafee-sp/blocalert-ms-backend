package com.blocalert.notification.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DistributedRateLimiter {

    private final ProxyManager<String> proxyManager;
    private final BucketConfiguration emailBucketConfiguration;
    private final BucketConfiguration smsBucketConfiguration;

    private final static String EMAIL_BUCKET = "notification:rate-limit:email";
    private final static String SMS_BUCKET = "notification:rate-limit:sms";

    public void consumeEmail() throws InterruptedException {
        consume(EMAIL_BUCKET, emailBucketConfiguration);
    }

    public void consumeSms() throws InterruptedException {
        consume(SMS_BUCKET, smsBucketConfiguration);
    }

    private void consume(String bucketKey, BucketConfiguration bucketConfiguration) throws InterruptedException {

        Bucket bucket = proxyManager.builder()
                .build(
                        bucketKey,
                        () -> bucketConfiguration
                );

        bucket.asBlocking().consume(1);  // Throttle not reject
    }
}

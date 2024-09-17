package com.intuit.be_a_friend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RateLimiterConfig {

    private final Map<String, Bucket> buckets = new HashMap<>();


    public Bucket resolveBucket(String key) {
        // Create a new bucket with a rate limit of 10 requests per minute if one does not already exist
        return buckets.computeIfAbsent(key, k -> Bucket4j.builder()
                .addLimit(Bandwidth.simple(1000, Duration.ofMinutes(1)))
                .build());
    }

}

package com.intuit.be_a_friend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RateLimiterConfig {

    private final Map<String, Bucket> ipBuckets = new HashMap<>();
    private final Map<String, Bucket> userBuckets = new HashMap<>();

    public Bucket resolveIpBucket(String ip) {
        // Create a new bucket with a rate limit of 100 requests per minute if one does not already exist
        return ipBuckets.computeIfAbsent(ip, k -> Bucket4j.builder()
                .addLimit(Bandwidth.simple(100, Duration.ofMinutes(1)))
                .build());
    }

    public Bucket resolveUserBucket(String username) {
        // Create a new bucket with a rate limit of 10 requests per minute if one does not already exist
        return userBuckets.computeIfAbsent(username, k -> Bucket4j.builder()
                .addLimit(Bandwidth.simple(10, Duration.ofMinutes(1)))
                .build());
    }
}
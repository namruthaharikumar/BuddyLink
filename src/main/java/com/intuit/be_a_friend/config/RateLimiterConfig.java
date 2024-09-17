package com.intuit.be_a_friend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RateLimiterConfig {

    private final Map<String, Bucket> buckets = new HashMap<>();

    @Bean
    public void configureRateLimiters() {
        // Example rate limits for different endpoints
        buckets.put("/api/classA/endpoint1", Bucket4j.builder()
                .addLimit(Bandwidth.simple(10, Duration.ofMinutes(1)))
                .build());

        buckets.put("/api/classA/endpoint2", Bucket4j.builder()
                .addLimit(Bandwidth.simple(15, Duration.ofMinutes(1)))
                .build());

        buckets.put("/api/classB/endpoint1", Bucket4j.builder()
                .addLimit(Bandwidth.simple(5, Duration.ofMinutes(1)))
                .build());

        buckets.put("/api/classB/endpoint2", Bucket4j.builder()
                .addLimit(Bandwidth.simple(20, Duration.ofMinutes(1)))
                .build());
    }

    public Bucket getBucket(String endpoint) {
        return buckets.get(endpoint);
    }

}

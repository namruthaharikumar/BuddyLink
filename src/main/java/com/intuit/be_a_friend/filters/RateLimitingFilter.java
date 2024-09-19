package com.intuit.be_a_friend.filters;

import com.intuit.be_a_friend.config.RateLimiterConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimiterConfig rateLimiterConfig;

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String fieldValue = request.getRemoteAddr(); // For IP address
        logger.info("Incoming request from IP: {}", fieldValue);

        // Get or create a bucket based on the field value
        Bucket bucket = rateLimiterConfig.resolveBucket(fieldValue);

        if (bucket.tryConsume(1)) {
            logger.info("Request allowed for IP: {}", fieldValue);
            // Allow the request
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            logger.warn("Rate limit exceeded for IP: {}", fieldValue);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Please try again later.");
        }
    }
}
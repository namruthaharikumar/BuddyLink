package com.intuit.be_a_friend.filters;


import com.intuit.be_a_friend.config.RateLimiterConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimiterConfig rateLimiterConfig;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String fieldValue = request.getRemoteAddr(); // For IP address

        // Get or create a bucket based on the field value
        Bucket bucket = rateLimiterConfig.resolveBucket(fieldValue);

        if (bucket.tryConsume(1)) {
            // Allow the request
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Please try again later.");
        }
    }
}
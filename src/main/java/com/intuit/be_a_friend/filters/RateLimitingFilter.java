package com.intuit.be_a_friend.filters;

import com.intuit.be_a_friend.config.RateLimiterConfig;
import com.intuit.be_a_friend.utils.JwtUtil;
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
    @Autowired
    JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ipAddress = request.getRemoteAddr();
        String authorizationHeader = request.getHeader("Authorization");
        String username = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(token);
        }
        // Apply IP-based rate limiting
        Bucket ipBucket = rateLimiterConfig.resolveIpBucket(ipAddress);
        if (!ipBucket.tryConsume(1)) {
            logger.warn("Rate limit exceeded for IP: {}", ipAddress);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded for IP. Please try again later.");
            return;
        }

        // Apply username-based rate limiting if username is present
        if (username != null) {
            Bucket userBucket = rateLimiterConfig.resolveUserBucket(username);
            if (!userBucket.tryConsume(1)) {
                logger.warn("Rate limit exceeded for user: {}", username);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded for user. Please try again later.");
                return;
            }
        }

        logger.info("Request allowed for IP: {} and user: {}", ipAddress, username);
        filterChain.doFilter(request, response);
    }
}
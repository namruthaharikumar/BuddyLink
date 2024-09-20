package com.intuit.be_a_friend.filters;

import com.intuit.be_a_friend.config.RateLimiterConfig;
import com.intuit.be_a_friend.utils.JwtUtil;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RateLimitingFilterTest {

    @Mock
    private RateLimiterConfig rateLimiterConfig;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Bucket bucket;

    @InjectMocks
    private RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDoFilterInternal_AllowedRequest() throws ServletException, IOException {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(jwtUtil.extractUsername("validToken")).thenReturn("validUser");
        when(rateLimiterConfig.resolveIpBucket("127.0.0.1")).thenReturn(bucket);
        when(rateLimiterConfig.resolveUserBucket("validUser")).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, times(0)).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void testDoFilterInternal_RateLimitExceededForIP() throws ServletException, IOException {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimiterConfig.resolveIpBucket("127.0.0.1")).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(false);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
        verify(response, times(1)).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        assertEquals("Rate limit exceeded for IP. Please try again later.", stringWriter.toString().trim());
    }

    @Test
    void testDoFilterInternal_RateLimitExceededForUser() throws ServletException, IOException {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(jwtUtil.extractUsername("validToken")).thenReturn("validUser");
        when(rateLimiterConfig.resolveIpBucket("127.0.0.1")).thenReturn(bucket);
        when(rateLimiterConfig.resolveUserBucket("validUser")).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true).thenReturn(false);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
        verify(response, times(1)).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        assertEquals("Rate limit exceeded for user. Please try again later.", stringWriter.toString().trim());
    }
}
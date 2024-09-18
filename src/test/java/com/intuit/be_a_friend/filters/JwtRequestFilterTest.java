package com.intuit.be_a_friend.filters;

import com.intuit.be_a_friend.DTO.UserDTO;
import com.intuit.be_a_friend.services.UserService;
import com.intuit.be_a_friend.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.Mockito.*;

class JwtRequestFilterTest {

    @Mock
    private UserService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDoFilterInternal_AllowedEndpoint() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/user/signin");

        jwtRequestFilter.doFilterInternal(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ValidToken() throws ServletException, IOException {
        String token = "validToken";
        String username = "testuser";

        when(request.getRequestURI()).thenReturn("/api/v1/user/signin");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(token, username)).thenReturn(true);
        when(userDetailsService.getUserInformation(username)).thenReturn(new UserDTO());

        jwtRequestFilter.doFilterInternal(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
        String token = "invalidToken";
        String username = "testuser";

        when(request.getRequestURI()).thenReturn("/some-endpoint");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(token, username)).thenReturn(false);
        when(userDetailsService.getUserInformation(username)).thenReturn(new UserDTO());

        jwtRequestFilter.doFilterInternal(request, response, chain);

        verify(chain, times(0)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ExpiredToken() throws ServletException, IOException {
        String token = "expiredToken";

        when(request.getRequestURI()).thenReturn("/some-endpoint");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        jwtRequestFilter.doFilterInternal(request, response, chain);

        verify(chain, times(0)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_NoToken() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/some-endpoint");
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtRequestFilter.doFilterInternal(request, response, chain);

        verify(chain, times(0)).doFilter(request, response);
    }
}
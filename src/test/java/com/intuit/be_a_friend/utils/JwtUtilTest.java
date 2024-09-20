package com.intuit.be_a_friend.utils;

import com.intuit.be_a_friend.exceptions.InsufficientInformationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private Key secretKey;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Ensure key is secure
        jwtUtil.SECRET_KEY = secretKey;
    }

    @Test
    void testExtractUsername() {
        String token = Jwts.builder()
                .setSubject("testUser")
                .signWith(secretKey)
                .compact();

        String username = jwtUtil.extractUsername(token);
        assertEquals("testUser", username);
    }

    @Test
    void testExtractExpiration() {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 1000 * 60 * 60); // 1 hour later

        String token = Jwts.builder()
                .setExpiration(expiration)
                .signWith(secretKey)
                .compact();

        Date extractedExpiration = jwtUtil.extractExpiration(token);
        assertEquals(expiration.getTime() / 1000, extractedExpiration.getTime() / 1000);
    }

    @Test
    void testGenerateToken() {
        String username = "testUser";
        String token = jwtUtil.generateToken(username);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(username, claims.getSubject());
    }

    @Test
    void testValidateToken() {
        String username = "testUser";
        String token = jwtUtil.generateToken(username);

        boolean isValid = jwtUtil.validateToken(token, username);
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidUsername() {
        String username = "testUser";
        String token = jwtUtil.generateToken(username);

        boolean isValid = jwtUtil.validateToken(token, "invalidUser");
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_ExpiredToken() {
        String username = "testUser";
        String token = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // Expired 1 second ago
                .signWith(secretKey)
                .compact();

         assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.validateToken(token, username);
        });
    }
}
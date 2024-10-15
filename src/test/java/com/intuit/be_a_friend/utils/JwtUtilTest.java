package com.intuit.be_a_friend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtilTest {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private String SECRET_KEY = "mysecretkey";

    // Extract Username from Token
    public String extractUsername(String token) {
        logger.info("Extracting username from token");
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration date from Token
    public Date extractExpiration(String token) {
        logger.info("Extracting expiration date from token");
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        logger.info("Extracting claim from token");
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        logger.info("Extracting all claims from token");
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        logger.info("Checking if token is expired");
        return extractExpiration(token).before(new Date());
    }

    // Generate Token
    public String generateToken(String username) {
        logger.info("Generating token for username: {}", username);
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        logger.info("Creating token for subject: {}", subject);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours expiry
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // Validate Token
    public Boolean validateToken(String token, String username) {
        logger.info("Validating token for username: {}", username);
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}
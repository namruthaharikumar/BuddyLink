package com.intuit.be_a_friend.services;

import com.intuit.be_a_friend.entities.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class BasicAuthManager implements AuthenticationManager {

    @Autowired
    UserService userDetailsService;
    @Autowired
    PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(BasicAuthManager.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        logger.info("Authenticating user: {}", username);

        final UserInfo userDetails = userDetailsService.getUserInfo(username);
        if (userDetails == null) {
            logger.error("Invalid username: {}", username);
            throw new AuthenticationException("Invalid username or password") {};
        }
        if (passwordEncoder.matches(password, userDetails.getPassword())) {
            logger.info("Authentication successful for user: {}", username);
            return new UsernamePasswordAuthenticationToken(username, password, null);
        } else {
            logger.error("Invalid password for user: {}", username);
            throw new AuthenticationException("Invalid username or password") {};
        }
    }
}
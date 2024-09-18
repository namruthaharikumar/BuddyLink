package com.intuit.be_a_friend.services;

import com.intuit.be_a_friend.entities.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BasicAuthManager implements AuthenticationManager {
    @Autowired
    UserService userDetailsService;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        final UserInfo userDetails = userDetailsService.getUserInfo(username);
        if(userDetails == null) {
            throw new AuthenticationException("Invalid username or password") {
            };
        }
        if(passwordEncoder.matches(password,userDetails.getPassword())) {
            return new UsernamePasswordAuthenticationToken(username, password, null);
        } else {
            throw new AuthenticationException("Invalid username or password") {
            };
        }

    }
}

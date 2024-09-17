package com.intuit.be_a_friend.services;

import com.intuit.be_a_friend.entities.UserInfo;
import com.intuit.be_a_friend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {


    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private UserService userDetailsService;

    public String authenticate(String username, String password) throws Exception {
        try {
            // Step 2: Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Step 5: Load user details for further processing (optional)


            // Step 6: Generate and return JWT token
            return jwtTokenUtil.generateToken(authentication.getName());

        } catch (AuthenticationException e) {
            throw new Exception("Invalid username or password");
        }
    }
}

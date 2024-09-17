package com.intuit.be_a_friend.controllers;

import com.intuit.be_a_friend.DTO.AuthenticationRequestDTO;
import com.intuit.be_a_friend.services.BasicAuthManager;
import com.intuit.be_a_friend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user/signin")
public class AuthController {
    @Autowired
    BasicAuthManager basicAuthManager;
    @Autowired
    JwtUtil jwtTokenUtil;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping
    String getJWTToken(@RequestBody AuthenticationRequestDTO authenticationRequest) {
        Authentication authentication = basicAuthManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        String token =  jwtTokenUtil.generateToken(authentication.getName());
        return ("Bearer " + token);
    }
}

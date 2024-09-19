package com.intuit.be_a_friend.controllers;

import com.intuit.be_a_friend.DTO.AuthenticationRequestDTO;
import com.intuit.be_a_friend.DTO.UserDTO;
import com.intuit.be_a_friend.services.BasicAuthManager;
import com.intuit.be_a_friend.services.UserService;
import com.intuit.be_a_friend.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    BasicAuthManager basicAuthManager;

    @Autowired
    JwtUtil jwtTokenUtil;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/signup")
    @Operation(summary = "Signup a new user")
    public ResponseEntity<String> signup(@RequestBody UserDTO userDTO) throws Exception {
        logger.info("Signup request received for user: {}", userDTO.getUsername());
        userService.signup(userDTO);
        logger.info("User '{}' successfully signed up.", userDTO.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully created");

    }

    @PostMapping("/signin")
    @Operation(summary = "Signin a user")
    public ResponseEntity<String> getJWTToken(@RequestBody AuthenticationRequestDTO authenticationRequest) {
        logger.info("Signin request received for user: {}", authenticationRequest.getUsername());
            Authentication authentication = basicAuthManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );
            String token = jwtTokenUtil.generateToken(authentication.getName());
            logger.info("User '{}' successfully signed in and JWT generated.", authentication.getName());
            return ResponseEntity.status(HttpStatus.OK).body(token);

    }

}
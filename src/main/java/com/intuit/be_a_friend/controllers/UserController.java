package com.intuit.be_a_friend.controllers;

import com.intuit.be_a_friend.DTO.AuthenticationRequestDTO;
import com.intuit.be_a_friend.DTO.UserDTO;
import com.intuit.be_a_friend.entities.UserInfo;
import com.intuit.be_a_friend.services.BasicAuthManager;
import com.intuit.be_a_friend.services.UserService;
import com.intuit.be_a_friend.utils.JwtUtil;
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
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);


    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserDTO userDTO) throws Exception {
        userService.signup(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully created");
    }

    @PostMapping("/signin")
    String getJWTToken(@RequestBody AuthenticationRequestDTO authenticationRequest) {
         Authentication authentication = basicAuthManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        String token =  jwtTokenUtil.generateToken(authentication.getName());
        return ("Bearer " + token);
    }

    @GetMapping("/follow/{followUser}")
    public ResponseEntity<String> followUser(@PathVariable String followUser, @RequestHeader("Authorization") String token) {
        String username = jwtTokenUtil.extractUsername(token.substring(7));
        userService.followUser(username,followUser);
        return ResponseEntity.status(HttpStatus.OK).body("User successfully followed");
    }
}

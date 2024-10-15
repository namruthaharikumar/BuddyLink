package com.intuit.be_a_friend.controllers;


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
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user/followAction")
public class FollowController {
    @Autowired
    UserService userService;

    @Autowired
    BasicAuthManager basicAuthManager;

    @Autowired
    JwtUtil jwtTokenUtil;

    private static final Logger logger = LoggerFactory.getLogger(FollowController.class);

    @GetMapping("/{followUser}")
    @Operation(summary = "Follow another user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> followUser(
            @PathVariable String followUser,
            @RequestHeader(value = "Authorization") String token
    ) {
        String username = jwtTokenUtil.extractUsername(token.substring(7));
        logger.info("User '{}' is attempting to follow '{}'", username, followUser);
         userService.followUser(username, followUser);
         logger.info("User '{}' successfully followed '{}'", username, followUser);
         return ResponseEntity.status(HttpStatus.OK).body("User successfully followed");

    }

    @DeleteMapping("/{unfollowUser}")
    @Operation(summary = "Unfollow another user")
    public ResponseEntity<String> unfollowUser(
            @PathVariable String unfollowUser,
            @RequestHeader(value = "Authorization") String token
    ) {
        String username = jwtTokenUtil.extractUsername(token.substring(7));
        logger.info("User '{}' is attempting to unfollow '{}'", username, unfollowUser);
        userService.unfollowUser(username, unfollowUser);
        logger.info("User '{}' successfully unfollowed '{}'", username, unfollowUser);
        return ResponseEntity.status(HttpStatus.OK).body("User successfully unfollowed");
    }
    @GetMapping("/getFollowers")
    @Operation(summary = "Get all followers of a user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<String>> getFollowers(
            @RequestHeader(value = "Authorization") String token
    ) {
        String username = jwtTokenUtil.extractUsername(token.substring(7));
        logger.info("User '{}' is attempting to get followers", username);
        List<String> followers = userService.getFollowers(username);
        logger.info("User '{}' successfully retrieved followers", username);
        return ResponseEntity.status(HttpStatus.OK).body(followers);

    }
}

package com.intuit.be_a_friend.controllers;

import com.intuit.be_a_friend.DTO.UserDTO;
import com.intuit.be_a_friend.entities.Post;
import com.intuit.be_a_friend.entities.UserInfo;
import com.intuit.be_a_friend.exceptions.AccessDeniedException;
import com.intuit.be_a_friend.services.PostService;
import com.intuit.be_a_friend.services.UserService;
import com.intuit.be_a_friend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/list")
    public Page<Post> getPostsByUserIds(@RequestHeader("Authorization") String token, Integer pageNumber) {
        String username = jwtUtil.extractUsername(token.substring(7));
         UserInfo userInfo =  userService.getUserInfoByUserName(username);
        if (userInfo == null) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found");
        }
        Pageable pageable = Pageable.ofSize(10).withPage(pageNumber);
         return postService.getPostsByUserIdsInReverseChronologicalOrder(userInfo.getUserId(), pageable);
    }
    @PostMapping("/create")
    ResponseEntity<String> createPost(@RequestHeader("Authorization") String token, @RequestBody String post) {
        String username = jwtUtil.extractUsername(token.substring(7));
        postService.createPost(username, post);
        return ResponseEntity.ok("Post created successfully");
    }

    @DeleteMapping("/delete/{postId}")
    ResponseEntity<String> deletePost(@RequestHeader("Authorization") String token, @PathVariable Long postId) throws AccessDeniedException {
        String username = jwtUtil.extractUsername(token.substring(7));
        postService.deletePost(username, postId);
        return ResponseEntity.ok("Post deleted successfully");
    }

    @PutMapping("/update/{postId}")
    ResponseEntity<String> updatePost(@RequestHeader("Authorization") String token, @PathVariable Long postId, @RequestBody String post) throws AccessDeniedException {
        String username = jwtUtil.extractUsername(token.substring(7));
        postService.updatePost(username, postId, post);
        return ResponseEntity.ok("Post updated successfully");
    }
}
package com.intuit.be_a_friend.controllers;

import com.intuit.be_a_friend.DTO.CommentReplyRequestDTO;
import com.intuit.be_a_friend.DTO.CommentRequestDTO;
import com.intuit.be_a_friend.DTO.CommentResponseDTO;
import com.intuit.be_a_friend.entities.Comment;
import com.intuit.be_a_friend.entities.UserInfo;
import com.intuit.be_a_friend.exceptions.AccessDeniedException;
import com.intuit.be_a_friend.exceptions.CommentNotFoundException;
import com.intuit.be_a_friend.exceptions.PostNotFoundException;
import com.intuit.be_a_friend.services.CommentService;
import com.intuit.be_a_friend.services.UserService;
import com.intuit.be_a_friend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentResponseDTO> addComment(@RequestHeader("Authorization") String token,@PathVariable Long postId, @RequestBody CommentRequestDTO request) throws PostNotFoundException, CommentNotFoundException {
        String username = jwtUtil.extractUsername(token.substring(7));
        UserInfo userInfo =  userService.getUserInfoByUserName(username);
        if (userInfo == null) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found");
        }
        CommentResponseDTO comment = commentService.addComment(request.getRequestId(),postId, userInfo.getUserId(),
                request.getContent());
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommentResponseDTO> addComment(@RequestHeader("Authorization") String token,@PathVariable Long commentId) throws PostNotFoundException, CommentNotFoundException, AccessDeniedException {
        String username = jwtUtil.extractUsername(token.substring(7));
        UserInfo userInfo =  userService.getUserInfoByUserName(username);
        if (userInfo == null) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found");
        }
         commentService.deleteComment(commentId, userInfo.getUserId());
        return ResponseEntity.ok().build();
    }


    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDTO> updateComment(@RequestHeader("Authorization") String token, @PathVariable Long commentId, @RequestBody CommentRequestDTO request) throws CommentNotFoundException, AccessDeniedException {
        String username = jwtUtil.extractUsername(token.substring(7));
        UserInfo userInfo = userService.getUserInfoByUserName(username);
        if (userInfo == null) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found");
        }

        CommentResponseDTO updatedComment = commentService.updateComment(commentId, userInfo.getUserId(), request.getContent());
        return ResponseEntity.ok(updatedComment);
    }

    @GetMapping("/post/{postId}")
    public Page<CommentResponseDTO> getTopLevelComments(@PathVariable Long postId, @RequestParam Integer pageId, @RequestParam boolean isRefresh) throws PostNotFoundException {
        if(isRefresh) {
            commentService.evictPostCache(postId,pageId);
        }
        return commentService.getTopLevelComments(postId,pageId);
    }

    @GetMapping("/{commentId}/replies")
    public Page<CommentResponseDTO> getReplies(@PathVariable Long commentId,
                                                    @RequestParam int page) throws CommentNotFoundException {
        return commentService.getReplies(commentId, page);
    }
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<CommentResponseDTO> addReply(@RequestHeader("Authorization") String token,@PathVariable Long commentId, @RequestBody CommentRequestDTO request) throws CommentNotFoundException {
        String username = jwtUtil.extractUsername(token.substring(7));
        UserInfo userInfo =  userService.getUserInfoByUserName(username);
        if (userInfo == null) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found");
        }
        return ResponseEntity.ok(commentService.addReply(commentId, userInfo.getUserId(), request.getContent()));
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<Void> likeComment(@RequestHeader("Authorization") String token, @PathVariable Long commentId) throws CommentNotFoundException {
        String username = jwtUtil.extractUsername(token.substring(7));
        UserInfo userInfo =  userService.getUserInfoByUserName(username);
        if (userInfo == null) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found");
        }
        commentService.likeOrDislikeComment(commentId,userInfo,true);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{commentId}/like")
    public ResponseEntity<List<String>> getLikeOfComment(@PathVariable Long commentId, @RequestParam int page) {
        return ResponseEntity.ok(commentService.getLikesOrDisLikeOfComment(commentId,true, page));
    }

    @PostMapping("/{commentId}/dislike")
    public ResponseEntity<Void> dislikeComment(@RequestHeader("Authorization") String token, @PathVariable Long commentId) throws CommentNotFoundException {
        String username = jwtUtil.extractUsername(token.substring(7));
        UserInfo userInfo =  userService.getUserInfoByUserName(username);
        if (userInfo == null) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found");
        }
        commentService.likeOrDislikeComment(commentId,userInfo,false);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{commentId}/dislike")
    public ResponseEntity<List<String>> getdisLikeOfComment(@PathVariable Long commentId,@RequestParam int page) {
        return ResponseEntity.ok(commentService.getLikesOrDisLikeOfComment(commentId,false, page));
    }


    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<Void> undoLike(@RequestHeader("Authorization") String token, @PathVariable Long commentId) throws AccessDeniedException {
        String username = jwtUtil.extractUsername(token.substring(7));
        UserInfo userInfo = userService.getUserInfoByUserName(username);
        if (userInfo == null) {
            throw new IllegalArgumentException("User not found");
        }

        commentService.undoLikeOrDislike(commentId, userInfo.getUserId(), true);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}/dislike")
    public ResponseEntity<Void> undoDislike(@RequestHeader("Authorization") String token, @PathVariable Long commentId) throws AccessDeniedException {
        String username = jwtUtil.extractUsername(token.substring(7));
        UserInfo userInfo = userService.getUserInfoByUserName(username);
        if (userInfo == null) {
            throw new IllegalArgumentException("User not found");
        }

        commentService.undoLikeOrDislike(commentId, userInfo.getUserId(), false);
        return ResponseEntity.ok().build();
    }


}


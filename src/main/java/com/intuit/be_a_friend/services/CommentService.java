package com.intuit.be_a_friend.services;

import com.intuit.be_a_friend.DTO.CommentResponseDTO;
import com.intuit.be_a_friend.entities.Comment;
import com.intuit.be_a_friend.entities.LikeEO;
import com.intuit.be_a_friend.entities.Post;
import com.intuit.be_a_friend.entities.UserInfo;
import com.intuit.be_a_friend.exceptions.AccessDeniedException;
import com.intuit.be_a_friend.exceptions.CommentNotFoundException;
import com.intuit.be_a_friend.exceptions.PostNotFoundException;
import com.intuit.be_a_friend.repositories.CommentRepository;
import com.intuit.be_a_friend.repositories.LikeRepository;
import com.intuit.be_a_friend.repositories.PostRepository;
import com.intuit.be_a_friend.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LikeRepository likeRepository;

    private static final int MAX_DEPTH = 100;
    private static final int MAX_COMMENTS_PER_LEVEL = 1000;
    @Autowired
    private PostService postService;


    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Transactional
    @Operation(summary = "Add a comment to a post", description = "Adds a comment to a post. If parentCommentId is provided, the comment is treated as a reply.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment added successfully"),
            @ApiResponse(responseCode = "404", description = "Post or parent comment not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input or maximum depth/level reached")
    })
    public CommentResponseDTO addComment(
            @Parameter(description = "Request id of the comment") String requestId,
            @Parameter(description = "ID of the post to comment on") Long postId,
            @Parameter(description = "ID of the user adding the comment") String userId,
            @Parameter(description = "Content of the comment") String content
    ) throws  PostNotFoundException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(String.format("Post %s not found", postId)));

        if(commentRepository.findByRequestIdAndUserIdAndPostId(requestId,userId,postId).isPresent()){
            throw new IllegalArgumentException("Duplicate comment request");
        }
        Comment comment = new Comment();
        comment.setRequestId(requestId);
        comment.setContent(content);
        comment.setPost(post);
        comment.setUser(userRepository.findUserInfoByUserId(userId));  // Assuming user exists

        // Handle replies (non-top-level comments)
            // Top-level comment: set depth to 0
            comment.setDepth(0);

            // Check if there are more than 1000 top-level comments
            int topLevelCommentCount = commentRepository.countByPostAndParentCommentIsNull(post);
            if (topLevelCommentCount >= MAX_COMMENTS_PER_LEVEL) {
                throw new IllegalArgumentException("Maximum 1000 top-level comments reached.");
            }
        postService.commentPost(userId,postId);
        return commentResponsetDTO(commentRepository.save(comment));
    }

    @Transactional
    @Operation(summary = "Delete a comment", description = "Deletes a comment by its ID if it belongs to the user making the request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Comment not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to delete this comment")
    })
    public void deleteComment(
            @Parameter(description = "ID of the comment to delete") Long commentId,
            @Parameter(description = "ID of the user making the request") String userId
    ) throws CommentNotFoundException, AccessDeniedException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(String.format("Comment id %s not found", commentId)));

        if (!comment.getUser().getUserId().equals(userId) && !comment.getPost().getUserId().equals(userId)) {
            throw new AccessDeniedException("User not authorized to delete this comment");
        }
        evictPostCache(comment.getPost().getId(),0);
        evictPostCache(comment.getPost().getId(),1);
        commentRepository.delete(comment);

    }

    @Transactional
    @Operation(summary = "Update a comment", description = "Updates a comment by its ID if it belongs to the user making the request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
            @ApiResponse(responseCode = "404", description = "Comment not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to update this comment")
    })
    public CommentResponseDTO updateComment(
            @Parameter(description = "ID of the comment to update") Long commentId,
            @Parameter(description = "ID of the user making the request") String userId,
            @Parameter(description = "New content for the comment") String newContent
    ) throws CommentNotFoundException, AccessDeniedException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(String.format("Comment id %s not found", commentId)));

        if (!comment.getUser().getUserId().equals(userId) && !comment.getPost().getUserId().equals(userId)) {
            throw new AccessDeniedException("User not authorized to update this comment");
        }
        evictPostCache(comment.getPost().getId(),0);
        evictPostCache(comment.getPost().getId(),1);
        comment.setContent(newContent);
        commentRepository.save(comment);
        return commentResponsetDTO(comment);
    }


    @Operation(summary = "Get top-level comments for a post", description = "Fetches top-level comments for a given post ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @Cacheable(value = "topLevelComments", key = "#postId + '_' + #page")
    public Page<CommentResponseDTO> getTopLevelComments(
            @Parameter(description = "ID of the post") Long postId,
            @Parameter(description = "Page number") int page
    ) throws PostNotFoundException {
        Pageable pageable = PageRequest.of(page, 10);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(String.format("Post %s not found", postId)));
        Page<Comment> commentPage =  commentRepository.findByPostAndParentCommentIsNull(post, pageable);
        return commentPage.map(this::commentResponsetDTO);
    }

    @CacheEvict(value = "topLevelComments", key = "#postId + '_' + #page")
    public void evictPostCache(Long postId, int page) {
        logger.info("Comment cache is evicted for post id: {} and page: {}", postId, page);
    }

    @Operation(summary = "Get replies for a comment", description = "Fetches replies for a given comment ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Replies fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Parent comment not found")
    })
    public Page<CommentResponseDTO> getReplies(
            @Parameter(description = "ID of the parent comment") Long commentId,
            @Parameter(description = "Pagination information") int page
    ) throws CommentNotFoundException {
        Pageable pageable = PageRequest.of(page, 10);
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(String.format("Parent comment id %s not found", commentId)));
        return commentRepository.findByParentComment(parentComment, pageable).map(this::commentResponsetDTO);
    }

    @Operation(summary = "Like a comment", description = "Adds a like to a comment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment liked successfully"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @Transactional
    public void likeOrDislikeComment(
            @Parameter(description = "ID of the comment to like") Long commentId, @Parameter(description = "ID of the user liking the comment") UserInfo userId, boolean isLike
    ) throws CommentNotFoundException {
        likeRepository.findByCommentAndUser(commentId, userId.getUserId()).ifPresent(likeEO -> {
            throw new IllegalArgumentException("User has already liked/disliked this comment");
        });
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(String.format("Parent comment id %s not found", commentId)));
        comment.addLike();
        LikeEO likeEO = new LikeEO();
        likeEO.setComment(comment);
        likeEO.setUser(userId);
        likeEO.setLike(isLike);
        likeRepository.save(likeEO);
        commentRepository.save(comment);
    }






    protected CommentResponseDTO commentResponsetDTO(Comment comment) {
        CommentResponseDTO commentRequestDTO = new CommentResponseDTO();
        commentRequestDTO.setPostId(comment.getPost().getId());
        commentRequestDTO.setUserId(comment.getUser().getUserId());
        commentRequestDTO.setContent(comment.getContent());
        commentRequestDTO.setCommentId(comment.getId());
        if (comment.getParentComment() != null) {
            commentRequestDTO.setParentCommentId(comment.getParentComment().getId());
        }
        commentRequestDTO.setLikes(comment.getLikes());
        return commentRequestDTO;
    }

    public CommentResponseDTO addReply(Long commentId, String userId, String content) throws CommentNotFoundException {
        Optional<Comment> parentComment = Optional.ofNullable(commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(String.format("Parent comment id %s not found", commentId))));

        int depth = parentComment.get().getDepth() + 1;
        if (depth > MAX_DEPTH) {
            throw new IllegalArgumentException("Maximum comment depth of 100 reached.");
        }
        Comment comment = new Comment();
        comment.setDepth(depth);
        comment.setContent(content);
        comment.setPost(parentComment.get().getPost());
        comment.setUser(userRepository.findUserInfoByUserId(userId));
        // Check if the number of replies at this level exceeds 1000
        int commentsAtCurrentLevel = commentRepository.countByParentComment(parentComment.get());
        if (commentsAtCurrentLevel >= MAX_COMMENTS_PER_LEVEL) {
            throw new IllegalArgumentException("Maximum 1000 comments per depth level reached.");
        }

        comment.setParentComment(parentComment.get());
        return commentResponsetDTO(commentRepository.save(comment));

    }


    @org.springframework.transaction.annotation.Transactional
    @Operation(summary = "Undo like/dislike on a post", description = "Removes a like or dislike from a post if it belongs to the user making the request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like/Dislike removed successfully"),
            @ApiResponse(responseCode = "404", description = "Like/Dislike not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to remove this like/dislike")
    })
    public void undoLikeOrDislike(
            @Parameter(description = "ID of the post") Long commentId,
            @Parameter(description = "ID of the user making the request") String userId,
            @Parameter(description = "True if undoing a like, false if undoing a dislike") boolean isLike
    ) throws AccessDeniedException {
        LikeEO likeEO = likeRepository.findByCommentIdAndUserAndLike(commentId, userId, isLike)
                .orElseThrow(() -> new AccessDeniedException("Like/Dislike not found or user not authorized"));

        likeRepository.delete(likeEO);
    }



    public List<String> getLikesOrDisLikeOfComment(Long commentId,boolean isLike, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<LikeEO> likeEOList =  likeRepository.findByCommentIdAndLike(commentId,isLike,pageable);
        return likeEOList.stream().map(obj -> obj.getUser().getUsername()).collect(Collectors.toList());
    }

    /*@PostConstruct
    public void postConstruct() {
        Post post = postRepository.findById(1L).orElseThrow(() -> new RuntimeException("Post not found"));
        UserInfo userInfo = userRepository.findUserInfoByUserId("01555c76-e193-4008-a79e-d0cf7ea28db0");

        List<Comment> previousLevelComments = new ArrayList<>();
        for (int depth = 0; depth < 100; depth++) {
            List<Comment> currentLevelComments = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                UserInfo user = userInfo;
                Comment comment = new Comment();
                comment.setContent("Comment at depth " + depth + " number " + (i + 1));
                comment.setPost(post);
                comment.setUser(user);
                comment.setDepth(depth);

                if (depth > 0) {
                    Comment parentComment = previousLevelComments.get(i % previousLevelComments.size());
                    comment.setParentComment(parentComment);
                }

                commentRepository.save(comment);
                currentLevelComments.add(comment);
            }
            previousLevelComments = currentLevelComments;
        }
    }*/
}
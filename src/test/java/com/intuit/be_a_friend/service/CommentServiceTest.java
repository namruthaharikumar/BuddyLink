package com.intuit.be_a_friend.service;

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
import com.intuit.be_a_friend.services.CommentService;
import com.intuit.be_a_friend.services.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    PostService postService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    PostRepository postRepository;


    @InjectMocks
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testAddComment() throws PostNotFoundException {
        String username = "testUser";
        String commentContent = "This is a test comment";

        UserInfo userInfo = getUserInfo();
        userInfo.setUsername(username);
        userInfo.setUserId("userId");
        Post post = getPost();
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(userInfo);

        when(userRepository.findByUsername(username)).thenReturn(userInfo);
        when(postRepository.findById(post.getId())).thenReturn(Optional.ofNullable(post));
        when(commentRepository.save(any())).thenReturn(comment);
        doNothing().when(postService).commentPost(username,post.getId());

        commentService.addComment(username, post.getId(), userInfo.getUserId(), commentContent);

        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testDeleteComment_Success() throws CommentNotFoundException, AccessDeniedException {
        Long commentId = 1L;
        String userId = "user123";
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(getUserInfo());
        comment.setPost(getPost());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.deleteComment(commentId, getUserInfo().getUserId());

        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void testDeleteComment_CommentNotFound() {
        Long commentId = 1L;
        String userId = "user123";

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.deleteComment(commentId, userId));
    }

    @Test
    void testDeleteComment_Unauthorized() {
        Long commentId = 1L;
        String userId = "user456";
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(getUserInfo());
        comment.setPost(getPost());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.deleteComment(commentId, userId));
    }

    @Test
    void testUpdateComment_Success() throws CommentNotFoundException, AccessDeniedException {
        Long commentId = 1L;
        String userId = "user123";
        String newContent = "Updated content";
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(getUserInfo());
        comment.setContent("Old content");
        comment.setPost(getPost());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        CommentResponseDTO response = commentService.updateComment(commentId, getUserInfo().getUserId(), newContent);

        assertEquals(newContent, response.getContent());
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    void testUpdateComment_CommentNotFound() {
        Long commentId = 1L;
        String userId = "user123";
        String newContent = "Updated content";

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.updateComment(commentId, userId, newContent));
    }

    @Test
    void testUpdateComment_Unauthorized() {
        Long commentId = 1L;
        String newContent = "Updated content";
        Comment comment = new Comment();
        comment.setPost(getPost());
        comment.setId(commentId);
        comment.setUser(getUserInfo());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.updateComment(commentId, "userId46", newContent));
    }

    @Test
    void testCreateComment_Success() throws CommentNotFoundException {
        Long commentId = 1L;
        String userId = "user123";
        String content = "New comment";
        Post post = getPost();
        UserInfo user = getUserInfo();
        Comment parentComment = new Comment();
        parentComment.setPost(post);

        when(userRepository.findUserInfoByUserId(userId)).thenReturn(user);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponseDTO response = commentService.addReply(commentId, userId, content);

        assertEquals(content, response.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testLikeComment_Success() throws CommentNotFoundException {
        Long commentId = 1L;
        String userId = "user123";
        Comment comment = getComment(commentId);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment ));
        when(likeRepository.save(any(LikeEO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        commentService.likeOrDislikeComment(commentId, getUserInfo(), true);

        verify(likeRepository, times(1)).save(any(LikeEO.class));
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    void testUnlikeComment_Success() throws AccessDeniedException {
        Long commentId = 1L;
        String userId = "user123";
        LikeEO likeEO = getLikeEO(commentId, userId, true);

        when(likeRepository.findByCommentIdAndUserAndLike(commentId, userId, true)).thenReturn(Optional.of(likeEO));

        commentService.undoLikeOrDislike(commentId, userId, true);

        verify(likeRepository, times(1)).delete(likeEO);
    }

    @Test
    void testDeleteLike_Success() throws AccessDeniedException {
        Long commentId = 1L;
        String userId = "user123";
        LikeEO likeEO = getLikeEO(commentId, userId, true);

        when(likeRepository.findByCommentIdAndUserAndLike(commentId, userId, true)).thenReturn(Optional.of(likeEO));

        commentService.undoLikeOrDislike(commentId, userId, true);

        verify(likeRepository, times(1)).delete(likeEO);
    }

    @Test
    void testDeleteDislike_Success() throws AccessDeniedException {
        Long commentId = 1L;
        String userId = "user123";
        LikeEO likeEO = getLikeEO(commentId, userId, false);

        when(likeRepository.findByCommentIdAndUserAndLike(commentId, userId, false)).thenReturn(Optional.of(likeEO));

        commentService.undoLikeOrDislike(commentId, userId, false);

        verify(likeRepository, times(1)).delete(likeEO);
    }

    @Test
    void testAddComment_PostNotFoundException() {
        String username = "testUser";
        Long postId = 1L;
        String commentContent = "This is a test comment";

        when(userRepository.findByUsername(username)).thenReturn(getUserInfo());
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> commentService.addComment(username, postId, "userId", commentContent));
    }

    @Test
    void testAddReply_CommentNotFoundException() {
        Long commentId = 1L;
        String userId = "user123";
        String content = "This is a reply";

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.addReply(commentId, userId, content));
    }

    @Test
    void testDeleteComment_CommentNotFoundException() {
        Long commentId = 1L;
        String userId = "user123";

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.deleteComment(commentId, userId));
    }

    @Test
    void testDeleteComment_AccessDeniedException() {
        Long commentId = 1L;
        String userId = "user123";
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(getUserInfo());
        comment.setPost(getPost());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.deleteComment(commentId, "differentUserId"));
    }

    @Test
    void testUpdateComment_CommentNotFoundException() {
        Long commentId = 1L;
        String userId = "user123";
        String newContent = "Updated content";

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.updateComment(commentId, userId, newContent));
    }

    @Test
    void testUpdateComment_AccessDeniedException() {
        Long commentId = 1L;
        String newContent = "Updated content";
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(getUserInfo());
        comment.setPost(getPost());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.updateComment(commentId, "differentUserId", newContent));
    }

    @Test
    void testUndoLikeOrDislike_AccessDeniedException() {
        Long commentId = 1L;
        String userId = "user123";
        boolean isLike = true;

        when(likeRepository.findByCommentIdAndUserAndLike(commentId, userId, isLike)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> commentService.undoLikeOrDislike(commentId, userId, isLike));
    }

    @Test
    void testAddComment_MaxTopLevelCommentsReached() {
        String username = "testUser";
        Long postId = 1L;
        String commentContent = "This is a test comment";

        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(username);
        userInfo.setUserId("userId");

        Post post = new Post();
        post.setId(postId);
        post.setUserId("userId");

        when(userRepository.findByUsername(username)).thenReturn(userInfo);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.countByPostAndParentCommentIsNull(post)).thenReturn(1000);

        assertThrows(IllegalArgumentException.class, () -> commentService.addComment("request-id", postId, userInfo.getUserId(), commentContent));
    }

    @Test
    void testGetTopLevelComments_Success() throws PostNotFoundException {
        Long postId = 1L;
        Post post = getPost();
        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setPost(post);
        comment1.setUser(getUserInfo());
        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setUser(getUserInfo());
        comment2.setPost(post);
        Pageable pageable = PageRequest.of(0, 10);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostAndParentCommentIsNull(post,pageable)).thenReturn((new PageImpl<>(Arrays.asList(comment1, comment2),pageable,2)));

        Page<CommentResponseDTO> topLevelComments = commentService.getTopLevelComments(postId, 0);

        assertEquals(2, topLevelComments.getTotalElements());
        verify(commentRepository, times(1)).findByPostAndParentCommentIsNull(post,pageable);
    }

    @Test
    void testGetReplies_Success() throws CommentNotFoundException {
        Long commentId = 1L;
        Comment parentComment = new Comment();
        parentComment.setId(commentId);
        Comment reply1 = new Comment();
        reply1.setId(2L);
        reply1.setUser(getUserInfo());
        reply1.setPost(getPost());
        reply1.setParentComment(parentComment);
        Comment reply2 = new Comment();
        reply2.setId(3L);
        reply2.setPost(getPost());
        reply2.setUser(getUserInfo());
        reply2.setParentComment(parentComment);
        Pageable pageable = PageRequest.of(0, 10);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(parentComment));
        when(commentRepository.findByParentComment(parentComment,pageable)).thenReturn((new PageImpl<>(Arrays.asList(reply1, reply2),pageable,2)));

        Page<CommentResponseDTO> replies = commentService.getReplies(commentId,0);

        assertEquals(2, replies.getTotalElements());
        verify(commentRepository, times(1)).findByParentComment(parentComment,pageable);
    }

    @Test
    public void testGetLikesOrDislikesOfComment() {
        // Step 1: Prepare the mock data
        UserInfo user1 = new UserInfo();
        user1.setUsername("user1");

        UserInfo user2 = new UserInfo();
        user2.setUsername("user2");

        LikeEO like1 = new LikeEO();
        like1.setUser(user1);
        like1.setLike(true);

        LikeEO like2 = new LikeEO();
        like2.setUser(user2);
        like2.setLike(true);

        List<LikeEO> likesList = new ArrayList<>();
        likesList.add(like1);
        likesList.add(like2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<LikeEO> likePage = new PageImpl<>(likesList, pageable, likesList.size());

        // Step 2: Mock the repository call
        when(likeRepository.findByCommentIdAndLike(anyLong(), anyBoolean(), any(Pageable.class)))
                .thenReturn(likePage);

        // Step 3: Call the method under test
        List<String> result = commentService.getLikesOrDisLikeOfComment(1L, true, 0);

        // Step 4: Verify the results
        assertEquals(2, result.size()); // Should return 2 usernames
        assertEquals("user1", result.get(0)); // First username should be "user1"
        assertEquals("user2", result.get(1)); // Second username should be "user2"
    }

    @Test
    public void testAddReplyMaxDepthExceeded() {
        // Setup: Mock a parent comment with depth 100
        Comment parentComment = new Comment();
        parentComment.setDepth(100);
        parentComment.setPost(new Post());

        // Mock the repository to return the parent comment
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(parentComment));

        // Action & Assert: Expect IllegalArgumentException to be thrown due to maximum depth reached
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.addReply(1L, "user-id", "reply content");
        });
    }

    @Test
    public void testAddCommentDuplicateRequest() {
        // Setup: Mock the repository to simulate a duplicate comment request
        Comment existingComment = new Comment();
        when(commentRepository.findByRequestIdAndUserIdAndPostId(anyString(), anyString(), anyLong()))
                .thenReturn(Optional.of(existingComment));
        when(postRepository.findById(any())).thenReturn(Optional.ofNullable(getPost()));

        // Action & Assert: Expect IllegalArgumentException to be thrown due to duplicate request
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.addComment("request-id", 1L, "user-id", "comment content");
        });
    }

    @Test
    public void testLikeOrDislikeCommentDuplicateLike() {
        // Setup: Mock the repository to simulate that the user has already liked/disliked the comment
        LikeEO existingLikeEO = new LikeEO();
        when(likeRepository.findByCommentAndUser(anyLong(), anyString())).thenReturn(Optional.of(existingLikeEO));

        // Action & Assert: Expect IllegalArgumentException to be thrown due to duplicate like/dislike
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.likeOrDislikeComment(1L, getUserInfo(), true);  // Trying to like the comment
        });
    }


    private Comment getComment(Long commentId) {
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(getUserInfo());
        comment.setPost(getPost());
        return comment;
    }

    private LikeEO getLikeEO(Long commentId, String userId, boolean isLike) {
        LikeEO likeEO = new LikeEO();
        likeEO.setComment(getComment(commentId));
        likeEO.setUser(getUserInfo());
        likeEO.setLike(isLike);
        return likeEO;
    }


    UserInfo getUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId("user123");
        return userInfo;
    }

    Post getPost() {
        Post post = new Post();
        post.setUserId("RandomUser");
        return post;
    }
}
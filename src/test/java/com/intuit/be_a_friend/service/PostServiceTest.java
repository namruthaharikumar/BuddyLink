package com.intuit.be_a_friend.service;

import com.intuit.be_a_friend.entities.Post;
import com.intuit.be_a_friend.entities.UserInfo;
import com.intuit.be_a_friend.exceptions.AccessDeniedException;
import com.intuit.be_a_friend.repositories.FollowerRepository;
import com.intuit.be_a_friend.repositories.PostRepository;
import com.intuit.be_a_friend.repositories.UserRepository;
import com.intuit.be_a_friend.services.PostService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowerRepository followerRepository;

    @InjectMocks
    private PostService postService;

    private UserInfo userInfo;
    private Post post;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        userInfo = new UserInfo();
        userInfo.setUserId("user123");
        userInfo.setUsername("testuser");

        post = new Post();
        post.setId(1L);
        post.setContent("Test Content");
        post.setUserId(userInfo.getUserId());

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void testGetPostsByUserIdsInReverseChronologicalOrder() {
        List<String> followerIds = Arrays.asList("user123", "user456");
        Page<Post> postsPage = new PageImpl<>(Arrays.asList(post));

        when(userRepository.findByUsername("testuser")).thenReturn(userInfo);
        when(followerRepository.findFollowingUsersBySubscriberId("user123")).thenReturn(followerIds);
        when(postRepository.findPostsByUserIdInOrderByCreatedAtDesc(any(), any())).thenReturn(postsPage);

        Page<Post> result = postService.getPostsByUserIdsInReverseChronologicalOrder("testuser", pageable);

        assertEquals(1, result.getTotalElements());
        verify(followerRepository, times(1)).findFollowingUsersBySubscriberId(any());
        verify(postRepository, times(1)).findPostsByUserIdInOrderByCreatedAtDesc(any(), any());
    }

    @Test
    void testCreatePost() {
        when(userRepository.findByUsername("testuser")).thenReturn(userInfo);

        postService.createPost("testuser", "New Post Content");

        verify(postRepository, times(1)).save(any(Post.class));
        verify(userRepository, times(1)).findByUsername("testuser");
    }


    @Test
    void testGetPostsByUserIdsInReverseChronologicalOrder_NoPostsFound() {
        List<String> followerIds = Arrays.asList("user123", "user456");
        Page<Post> postsPage = new PageImpl<>(Arrays.asList());

        when(userRepository.findByUsername("user123")).thenReturn(userInfo);
        when(followerRepository.findFollowingUsersBySubscriberId("user123")).thenReturn(followerIds);
        when(postRepository.findPostsByUserIdInOrderByCreatedAtDesc(followerIds, pageable)).thenReturn(postsPage);

        Page<Post> result = postService.getPostsByUserIdsInReverseChronologicalOrder("user123", pageable);

        assertEquals(0, result.getTotalElements());
        verify(followerRepository, times(1)).findFollowingUsersBySubscriberId("user123");
        verify(postRepository, times(1)).findPostsByUserIdInOrderByCreatedAtDesc(followerIds, pageable);
    }

    @Test
    void testCreatePost_UserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> {
            postService.createPost("testuser", "New Post Content");
        });
        verify(postRepository, times(0)).save(any(Post.class));
        verify(userRepository, times(1)).findByUsername("testuser");
    }
    @Test
    void testUpdatePost_Success() throws AccessDeniedException {
        Long postId = 1L;
        String postContent = "Updated content";

        UserInfo userInfo = getUserInfo();

        Post post = new Post();
        post.setUserId(userInfo.getUserId());
        post.setContent("Original content");

        when(userRepository.findByUsername(userInfo.getUsername())).thenReturn(userInfo);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.updatePost(userInfo.getUsername(), postId, postContent);

        verify(postRepository, times(1)).save(post);
        assertEquals(postContent, post.getContent());
    }

    @Test
    void testUpdatePost_UserNotFound() {
        String username = "testuser";
        Long postId = 1L;
        String postContent = "Updated content";

        when(userRepository.findByUsername(username)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            postService.updatePost(username, postId, postContent);
        });

        assertEquals("User not found", exception.getMessage());
        verify(postRepository, times(0)).save(any(Post.class));
    }

    @Test
    void testUpdatePost_PostNotFound() {
        String username = "testuser";
        Long postId = 1L;
        String postContent = "Updated content";

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(username);
        userInfo.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(userInfo);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            postService.updatePost(username, postId, postContent);
        });

        assertEquals("Post not found/ User is not eligible to access the post", exception.getMessage());
        verify(postRepository, times(0)).save(any(Post.class));
    }

    @Test
    void testUpdatePost_UserNotAuthorized() {
        Long postId = 1L;
        String postContent = "Updated content";

        UserInfo userInfo = getUserInfo();
        userInfo.setUsername(userInfo.getUsername());

        Post post = new Post();
        post.setId(postId);
        post.setUserId("usertest"); // Different user ID

        when(userRepository.findByUsername(userInfo.getUsername())).thenReturn(userInfo);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            postService.updatePost(userInfo.getUsername(), postId, postContent);
        });

        assertEquals("User is not authorized to update this post", exception.getMessage());
        verify(postRepository, times(0)).save(any(Post.class));
    }

    @Test
    void testDeletePost_Success() throws AccessDeniedException {
        Long postId = 1L;

        UserInfo userInfo = getUserInfo();

        Post post = new Post();
        post.setId(postId);
        post.setUserId(userInfo.getUserId());

        when(userRepository.findByUsername(getUserInfo().getUsername())).thenReturn(userInfo);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.deletePost(getUserInfo().getUsername(), postId);

        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void testDeletePost_UserNotFound() {
        String username = "testuser";
        Long postId = 1L;

        when(userRepository.findByUsername(username)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            postService.deletePost(username, postId);
        });

        assertEquals("User not found", exception.getMessage());
        verify(postRepository, times(0)).delete(any(Post.class));
    }

    @Test
    void testDeletePost_PostNotFound() {
        String username = "testuser";
        Long postId = 1L;

        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(userInfo);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            postService.deletePost(username, postId);
        });

        assertEquals("Post not found", exception.getMessage());
        verify(postRepository, times(0)).delete(any(Post.class));
    }

    @Test
    void testDeletePost_UserNotAuthorized() {
        UserInfo userInfo = getUserInfo();

        Post post = new Post();
        post.setId(1L);
        post.setUserId("DIfferentId"); // Different user ID

        when(userRepository.findByUsername(userInfo.getUsername())).thenReturn(userInfo);
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            postService.deletePost(userInfo.getUsername(), post.getId());
        });

        assertEquals("User is not authorized to delete this post", exception.getMessage());
        verify(postRepository, times(0)).delete(any(Post.class));
    }


    public UserInfo getUserInfo() {
        String username = "testuser";
        Long postId = 1L;

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(UUID.randomUUID().toString());
        userInfo.setUsername(username);
        return userInfo;

       // Different user ID
    }

}
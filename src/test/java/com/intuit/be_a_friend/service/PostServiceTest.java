package com.intuit.be_a_friend.service;

import com.intuit.be_a_friend.entities.Post;
import com.intuit.be_a_friend.entities.UserInfo;
import com.intuit.be_a_friend.repositories.FollowerRepository;
import com.intuit.be_a_friend.repositories.PostRepository;
import com.intuit.be_a_friend.repositories.UserRepository;
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

import java.util.Arrays;
import java.util.List;

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
        when(postRepository.findPostsByUserIdInOrderByCreatedAtDesc(followerIds, pageable)).thenReturn(postsPage);

        Page<Post> result = postService.getPostsByUserIdsInReverseChronologicalOrder("testuser", pageable);

        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(followerRepository, times(1)).findFollowingUsersBySubscriberId("user123");
        verify(postRepository, times(1)).findPostsByUserIdInOrderByCreatedAtDesc(followerIds, pageable);
    }

    @Test
    void testCreatePost() {
        when(userRepository.findByUsername("testuser")).thenReturn(userInfo);

        postService.createPost("testuser", "New Post Content");

        verify(postRepository, times(1)).save(any(Post.class));
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    //Add test cases for failure scenarios
    //1. testGetPostsByUserIdsInReverseChronologicalOrder_UserNotFound
    //2. testGetPostsByUserIdsInReverseChronologicalOrder_NoPostsFound
    //3. testCreatePost_UserNotFound
    @Test
    void testGetPostsByUserIdsInReverseChronologicalOrder_UserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> postService.getPostsByUserIdsInReverseChronologicalOrder("testuser", pageable));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(followerRepository, times(0)).findFollowingUsersBySubscriberId("user123");
        verify(postRepository, times(0)).findPostsByUserIdInOrderByCreatedAtDesc(any(), any());
    }

    @Test
    void testGetPostsByUserIdsInReverseChronologicalOrder_NoPostsFound() {
        List<String> followerIds = Arrays.asList("user123", "user456");
        Page<Post> postsPage = new PageImpl<>(Arrays.asList());

        when(userRepository.findByUsername("testuser")).thenReturn(userInfo);
        when(followerRepository.findFollowingUsersBySubscriberId("user123")).thenReturn(followerIds);
        when(postRepository.findPostsByUserIdInOrderByCreatedAtDesc(followerIds, pageable)).thenReturn(postsPage);

        Page<Post> result = postService.getPostsByUserIdsInReverseChronologicalOrder("testuser", pageable);

        assertEquals(0, result.getTotalElements());
        verify(userRepository, times(1)).findByUsername("testuser");
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
}
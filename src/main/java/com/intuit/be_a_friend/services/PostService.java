package com.intuit.be_a_friend.services;

import com.intuit.be_a_friend.entities.Post;
import com.intuit.be_a_friend.entities.UserInfo;
import com.intuit.be_a_friend.exceptions.AccessDeniedException;
import com.intuit.be_a_friend.repositories.FollowerRepository;
import com.intuit.be_a_friend.repositories.PostRepository;
import com.intuit.be_a_friend.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FollowerRepository followerRepository;

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    public Page<Post> getPostsByUserIdsInReverseChronologicalOrder(String userName, Pageable pageable) {
        logger.info("Entering getPostsByUserIdsInReverseChronologicalOrder with username: {}", userName);
        UserInfo userInfo = userRepository.findByUsername(userName);
        if (userInfo == null) {
            logger.error("User not found: {}", userName);
            throw new IllegalArgumentException("User not found");
        }
        List<String> followersIds = followerRepository.findFollowingUsersBySubscriberId(userInfo.getUserId());
        Page<Post> posts = postRepository.findPostsByUserIdInOrderByCreatedAtDesc(followersIds, pageable);
        logger.info("Exiting getPostsByUserIdsInReverseChronologicalOrder with {} posts on page {}", posts.getSize(), pageable.getPageNumber());
        return posts;
    }

    public void createPost(String username, String content) {
        logger.info("Entering createPost with username: {}", username);
        UserInfo userInfo = userRepository.findByUsername(username);
        if (userInfo == null) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found");
        }
        Post post = new Post();
        post.setContent(content);
        post.setUserId(userInfo.getUserId());
        postRepository.save(post);
        logger.info("Post created for user: {}", username);
    }

    public void deletePost(String username, Long postId) throws AccessDeniedException {
        logger.info("Entering deletePost with username: {} and postId: {}", username, postId);
        UserInfo userInfo = userRepository.findByUsername(username);
        if (userInfo == null) {
            logger.error("User not found: {}", username);
            throw new IllegalArgumentException("User not found");
        }
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            logger.error("Post not found: {}", postId);
            throw new EntityNotFoundException("Post not found");
        }
        if (!post.getUserId().equals(userInfo.getUserId())) {
            logger.error("User {} is not authorized to delete post {}", username, postId);
            throw new AccessDeniedException("User is not authorized to delete this post");
        }
        postRepository.delete(post);
        logger.info("Post deleted for user: {}", username);
    }

    public void updatePost(String username, Long postId, String postContent) throws AccessDeniedException {
        logger.info("Entering updatePost with username: {}, postId: {}", username, postId);
        UserInfo userInfo = userRepository.findByUsername(username);
        if (userInfo == null) {
            logger.error("User not found: {}", username);
            throw new EntityNotFoundException("User not found");
        }
        Post postEntity = postRepository.findById(postId).orElse(null);
        if (postEntity == null) {
            logger.error("Post not found: {}", postId);
            throw new EntityNotFoundException("Post not found/ User is not eligible to access the post");
        }
        if (!postEntity.getUserId().equals(userInfo.getUserId())) {
            logger.error("User {} is not authorized to update post {}", username, postId);
            throw new AccessDeniedException("User is not authorized to update this post");
        }
        postEntity.setContent(postContent);
        postRepository.save(postEntity);
        logger.info("Post updated for user: {}", username);
    }



  /*  @PostConstruct
    @Transactional
    public void init() {
        logger.info("Initializing posts for users");
        List<UserInfo> users = userRepository.findAll();
        List<Post> posts = new ArrayList<>();
        int postsPerUser = 1_000_000 / users.size();

        for (UserInfo user : users) {
            for (int i = 1; i <= postsPerUser; i++) {
                Post post = new Post(generateContent(i, user.getUsername()), user.getUserId());
                posts.add(post);

                if (posts.size() % 1000 == 0) {
                    postRepository.saveAll(posts);
                    posts.clear();
                }
            }
        }

        if (!posts.isEmpty()) {
            postRepository.saveAll(posts);
        }
        logger.info("Finished initializing posts for users");
    }

    private String generateContent(int index, String userName) {
        String baseContent = "This is a sample post content for user " + userName + " for " + index + " index. ";
        StringBuilder contentBuilder = new StringBuilder(baseContent);
        while (contentBuilder.length() < 1000) {
            contentBuilder.append(baseContent);
        }
        return contentBuilder.substring(0, 1000);
    }*/
}
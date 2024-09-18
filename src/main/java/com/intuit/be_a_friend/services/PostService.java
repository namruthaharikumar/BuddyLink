package com.intuit.be_a_friend.services;


import com.intuit.be_a_friend.entities.Post;
import com.intuit.be_a_friend.entities.UserInfo;
import com.intuit.be_a_friend.repositories.FollowerRepository;
import com.intuit.be_a_friend.repositories.PostRepository;
import com.intuit.be_a_friend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FollowerRepository followerRepository;

    public Page<Post> getPostsByUserIdsInReverseChronologicalOrder(String userName, Pageable pageable) {
        UserInfo userInfo = userRepository.findByUsername(userName);
        if(userInfo == null) {
            throw new IllegalArgumentException("User not found");
        }
        List<String> followersIds = followerRepository.findFollowingUsersBySubscriberId(userInfo.getUserId());
        return postRepository.findPostsByUserIdInOrderByCreatedAtDesc(followersIds, pageable);
    }

    public void createPost(String username, String content) {
        UserInfo userInfo = userRepository.findByUsername(username);
        if(userInfo == null) {
            throw new IllegalArgumentException("User not found");
        }
        Post post = new Post();
        post.setContent(content);
        post.setUserId(userInfo.getUserId());
        postRepository.save(post);
    }

    public void deletePost(String username, Long postId) {
        UserInfo userInfo = userRepository.findByUsername(username);
        if(userInfo == null) {
            throw new IllegalArgumentException("User not found");
        }
        Post post = postRepository.findById(postId).orElse(null);
        if(post == null) {
            throw new IllegalArgumentException("Post not found");
        }
        if(!post.getUserId().equals(userInfo.getUserId())) {
            throw new IllegalArgumentException("User is not authorized to delete this post");
        }
        postRepository.delete(post);
    }

    public void updatePost(String username, Long postId, String post) {
        UserInfo userInfo = userRepository.findByUsername(username);
        if(userInfo == null) {
            throw new IllegalArgumentException("User not found");
        }
        Post postEntity = postRepository.findById(postId).orElse(null);
        if(postEntity == null) {
            throw new IllegalArgumentException("Post not found");
        }
        if(!postEntity.getUserId().equals(userInfo.getUserId())) {
            throw new IllegalArgumentException("User is not authorized to update this post");
        }
        postEntity.setContent(post);
        postRepository.save(postEntity);
    }

    //For generating records in db
 /*   @PostConstruct
    @Transactional
    public void init() {
        List<UserInfo> users = userRepository.findAll();
        List<Post> posts = new ArrayList<>();
        Random random = new Random();

        for (int i = 1; i <= 1_000_000; i++) {
            UserInfo randomUser = users.get(random.nextInt(users.size()));
            Post post = new Post(generateContent(i), randomUser);
            posts.add(post);

            if (i % 1000 == 0) {
                postRepository.saveAll(posts);
                posts.clear();
            }
        }

        if (!posts.isEmpty()) {
            postRepository.saveAll(posts);
        }
    }

    private String generateContent(int index) {
        String baseContent = "";
        StringBuilder contentBuilder = new StringBuilder(baseContent);
        while (contentBuilder.length() < 1000) {
            contentBuilder.append("x");
        }
        return contentBuilder.substring(0, 1000);
    }*/
}
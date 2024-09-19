package com.intuit.be_a_friend.repositories;

import com.intuit.be_a_friend.entities.Post;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    @Query("SELECT p FROM Post p JOIN UserInfo u ON p.userId = u.userId WHERE u.userId IN (:userIds) ORDER BY p.createdAt DESC")
    Page<Post> findPostsByUserIdInOrderByCreatedAtDesc(@Param("userIds") List<String> userIds, Pageable pageable);
}

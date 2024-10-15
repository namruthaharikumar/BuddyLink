package com.intuit.be_a_friend.repositories;

import com.intuit.be_a_friend.entities.Comment;
import com.intuit.be_a_friend.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
        // Fetch top-level comments by post ID (where parentComment is null)
        Page<Comment> findByPostAndParentCommentIsNull(Post post, Pageable pageable);

        // Fetch replies by parent comment
        Page<Comment> findByParentComment(Comment parentComment, Pageable pageable);

        int countByParentComment(Comment parentComment);

        int countByPostAndParentCommentIsNull(Post post);

        @Query("SELECT c FROM Comment c WHERE c.requestId = :requestId AND c.user.userId = :userId AND c.post.id = :postId")
        Optional<Comment> findByRequestIdAndUserIdAndPostId(String requestId, String userId, Long postId);
}

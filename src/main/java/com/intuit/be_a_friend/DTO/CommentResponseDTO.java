package com.intuit.be_a_friend.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class CommentResponseDTO {
    private Long postId;
    private String userId;
    private String content;
    private Long parentCommentId;
    private Long commentId;
    private int likes;
}

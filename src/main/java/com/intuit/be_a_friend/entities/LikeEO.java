package com.intuit.be_a_friend.entities;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "likes")
public class LikeEO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private UserInfo user;

    @JoinColumn(name = "is_like")
    boolean isLike;

}

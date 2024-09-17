package com.intuit.be_a_friend.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Primary;

@Entity
@Data
@NoArgsConstructor
@Table(name = "followers", uniqueConstraints = @UniqueConstraint(columnNames = {"subscriberId", "followingId"}))
public class Follower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String subscriberId;
    String followingId;
}
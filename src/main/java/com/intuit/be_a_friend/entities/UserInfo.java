package com.intuit.be_a_friend.entities;

import com.intuit.be_a_friend.enums.AccountType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Data
@NoArgsConstructor
@Table(name = "user_info", indexes = @Index(name = "idx_username", columnList = "username"))
public class UserInfo implements Serializable {

    @Id
    private String userId;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, length = 50)
    private String email;

    @Column(unique = true, nullable = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private AccountType accountType = AccountType.PUBLIC;


    private int followersCount = 0;

    private int followingCount = 0;

    public UserInfo(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @PrePersist
    protected void onCreate() {
        if (userId == null) {
            userId = UUID.randomUUID().toString();
        }
    }
}
package com.intuit.be_a_friend.entities;

import com.intuit.be_a_friend.enums.AccountType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(unique = true , nullable = false, length = 50)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(unique = true, length = 50)
    private String email;
    @Column(unique = true, nullable = true)
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255) default 'PRIVATE'")
    private AccountType accountType;
    @Column
    private Long followersCount;
    @Column
    private Long followingCount;

}

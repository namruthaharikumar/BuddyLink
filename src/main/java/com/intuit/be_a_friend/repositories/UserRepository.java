package com.intuit.be_a_friend.repositories;

import com.intuit.be_a_friend.entities.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<UserInfo, Long> {
    boolean existsUserInfoByUsername(String username);
    boolean existsUserInfoByPhoneNumber(String phoneNumber);
    boolean existsUserInfoByEmail(String email);
    UserInfo findByUsername(String username);
}

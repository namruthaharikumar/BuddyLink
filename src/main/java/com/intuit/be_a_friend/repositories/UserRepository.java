package com.intuit.be_a_friend.repositories;

import com.intuit.be_a_friend.entities.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<UserInfo, Long> {
    boolean existsUserInfoByUsername(String username);
    boolean existsUserInfoByPhoneNumber(String phoneNumber);
    boolean existsUserInfoByEmail(String email);
    UserInfo findByUsername(String username);
    @Query("SELECT u.username from UserInfo u where u.userId in (:list)")
    List<String> getUserNameByIds(@Param("list") List<String> list);
    UserInfo findUserInfoByUserId(String userId);
}

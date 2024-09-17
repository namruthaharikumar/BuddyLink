package com.intuit.be_a_friend.repositories;

import com.intuit.be_a_friend.entities.Follower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, Long> {

    @Query("SELECT f.followingId FROM Follower f WHERE f.subscriberId = :subscriberId")
    List<String> findFollowingUsersBySubscriberId(String subscriberId);

}

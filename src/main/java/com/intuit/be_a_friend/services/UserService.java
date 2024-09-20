package com.intuit.be_a_friend.services;

import com.intuit.be_a_friend.DTO.UserDTO;
import com.intuit.be_a_friend.entities.Follower;
import com.intuit.be_a_friend.entities.UserInfo;
import com.intuit.be_a_friend.enums.OPERATION;
import com.intuit.be_a_friend.exceptions.DuplicateUserInformationException;
import com.intuit.be_a_friend.factory.ValidatorFactory;
import com.intuit.be_a_friend.repositories.FollowerRepository;
import com.intuit.be_a_friend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ValidatorFactory validatorFactory;
    @Autowired
    FollowerRepository followerRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    PostService postService;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserInfo signup(UserDTO userDTO) throws Exception {
        logger.info("Entering signup with userDTO: {}", userDTO);
        validatorFactory.getValidator(OPERATION.SIGNUP).validate(userDTO);
        UserInfo user = new UserInfo();

        if (userRepository.existsUserInfoByEmail(userDTO.getEmail())) {
            logger.error("Email already exists: {}", userDTO.getEmail());
            throw new DuplicateUserInformationException("Email already exists");
        }
        if (userDTO.getPhoneNumber() != null && userRepository.existsUserInfoByPhoneNumber(userDTO.getPhoneNumber())) {
            logger.error("Phone number already exists: {}", userDTO.getPhoneNumber());
            throw new DuplicateUserInformationException("Phone number already exists");
        }
        if (userRepository.existsUserInfoByUsername(userDTO.getUsername())) {
            logger.error("Username already exists: {}", userDTO.getUsername());
            throw new DuplicateUserInformationException("Username already exists");
        }
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        userRepository.save(user);
        logger.info("User successfully created: {}", userDTO.getUsername());
        return user;
    }

    public UserDTO getUserInformation(String username) {
        logger.info("Entering getUserInformation with username: {}", username);
        UserInfo user = userRepository.findByUsername(username);
        if (user == null) {
            logger.warn("User not found: {}", username);
            return null;
        }
        logger.info("User information retrieved for username: {}", username);
        return new UserDTO(user);
    }

    public UserInfo getUserInfoByUserName(String username) {
        logger.info("Entering getUserInformation with username: {}", username);
        UserInfo user = userRepository.findByUsername(username);
        if (user == null) {
            logger.warn("User not found: {}", username);
        } else {
            logger.info("User info retrieved for userName: {}", username);
        }
        return user;
    }

    public UserInfo getUserInfo(String username) {
        logger.info("Entering getUserInfo with username: {}", username);
        UserInfo user = userRepository.findByUsername(username);
        if (user == null) {
            logger.warn("User not found: {}", username);
        } else {
            logger.info("User info retrieved for username: {}", username);
        }
        return user;
    }

    public boolean followUser(String userName, String followerUserName) {
        logger.info("Entering followUser with userName: {} and followerUserName: {}", userName, followerUserName);
        if (userName.equals(followerUserName)) {
            logger.error("User cannot follow themselves: {}", userName);
            throw new IllegalArgumentException("User cannot follow themselves");
        }
        UserInfo userOpt = userRepository.findByUsername(userName);
        UserInfo followerOpt = userRepository.findByUsername(followerUserName);



        if (userOpt != null && followerOpt != null) {
            Follower followerObj = new Follower();
            userOpt.setFollowingCount(userOpt.getFollowingCount() + 1);
            followerOpt.setFollowersCount(followerOpt.getFollowersCount() + 1);
            followerObj.setSubscriberId(userOpt.getUserId());
            followerObj.setFollowingId(followerOpt.getUserId());
            followerRepository.save(followerObj);
            userRepository.saveAll(List.of(userOpt, followerOpt));
            postService.updateCache(userOpt.getUserId());
            logger.info("User {} successfully followed user {}", userName, followerUserName);
            return true;
        }

        logger.error("User not found: {} or {}", userName, followerUserName);
        throw new IllegalArgumentException("User not found");

    }

    public void unfollowUser(String username, String unfollowUser) {
        logger.info("Entering unfollowUser with username: {} and unfollowUser: {}", username, unfollowUser);
        UserInfo user = userRepository.findByUsername(username);
        UserInfo unfollowUserInfo = userRepository.findByUsername(unfollowUser);

        if (user == null || unfollowUserInfo == null) {
            logger.error("User not found: {} or {}", username, unfollowUser);
            throw new IllegalArgumentException("User not found");
        }

        postService.evictAllCacheForFollower(user.getUserId());

        Follower follower = followerRepository.findByFollowingIdAndSubscriberId(unfollowUserInfo.getUserId(), user.getUserId());
        if (follower != null) {
            followerRepository.delete(follower);
            user.setFollowingCount(user.getFollowingCount() - 1);
            unfollowUserInfo.setFollowersCount(unfollowUserInfo.getFollowersCount() - 1);
            userRepository.saveAll(List.of(user, unfollowUserInfo));
            logger.info("User {} successfully unfollowed user {}", username, unfollowUser);
        } else {
            logger.warn("No following relationship found between {} and {}", username, unfollowUser);
        }

    }

    public List<String> getFollowers(String username) {
        logger.info("Entering getFollowers with username: {}", username);
        UserInfo user = userRepository.findByUsername(username);
        if (user == null) {
            logger.warn("User not found: {}", username);
            throw new IllegalArgumentException("User not found");
        }
        List<String> followers = followerRepository.findFollowingUsersBySubscriberId(user.getUserId());
        List<String> followerUsernames = userRepository.getUserNameByIds(followers);
        logger.info("Followers retrieved for username: {}", username);
        return followerUsernames;
    }

    // Used for generating unique users
/*    @PostConstruct
    public void init() {
        logger.info("Initializing unique users");
        List<UserInfo> userInfoList = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            UserInfo userInfo = new UserInfo("user" + i, passwordEncoder.encode("password" + i));
            userInfoList.add(userInfo);
        }
        userRepository.saveAll(userInfoList);
        logger.info("Finished initializing unique users");
    }*/
}
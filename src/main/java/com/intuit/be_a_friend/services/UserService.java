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



    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    public UserInfo signup(UserDTO userDTO) throws Exception {
        validatorFactory.getValidator(OPERATION.SIGNUP).validate(userDTO);
        UserInfo user = new UserInfo();

        if(userRepository.existsUserInfoByEmail(userDTO.getEmail())) {
            throw new DuplicateUserInformationException("Email already exists");
        }
        if(userDTO.getPhoneNumber()!= null && !userRepository.existsUserInfoByPhoneNumber(userDTO.getPhoneNumber())) {
            throw new DuplicateUserInformationException("Phone number already exists");
        }
        if(userRepository.existsUserInfoByUsername(userDTO.getUsername())) {
            throw new DuplicateUserInformationException("Username already exists");
        }
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setAccountType(userDTO.getAccountType());
        userRepository.save(user);
        logger.info("User successfully created");
        return user;
    }
    public UserDTO  getUserInformation(String username) {
        UserInfo user = userRepository.findByUsername(username);
        if(user == null) {
            return null;
        }
        return new UserDTO(user);
    }

    public UserInfo getUserInfo(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean followUser(String userName, String follwerUserName) {
        if(userName.equals(follwerUserName)) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }
        UserInfo userOpt = userRepository.findByUsername(userName);
        UserInfo followerOpt = userRepository.findByUsername(follwerUserName);

        if (userOpt!=null && followerOpt!=null) {
            Follower followerObj = new Follower();
            userOpt.getFollowingCount().incrementAndGet();
            followerOpt.getFollowersCount().incrementAndGet();
            followerObj.setSubscriberId(userOpt.getUserId());
            followerObj.setFollowingId(followerOpt.getUserId());
            followerRepository.save(followerObj);
            userRepository.saveAll(List.of(userOpt, followerOpt));

            return true;
        }
        throw new IllegalArgumentException("User not found");
    }

    //Used for generating unique users
/*    @PostConstruct
    public void init() {
        List<UserInfo> userInfoList = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            UserInfo userInfo = new UserInfo("user" + i, passwordEncoder.encode("password" + i));
            userInfoList.add(userInfo);
        }
        userRepository.saveAll(userInfoList);
    }*/
}

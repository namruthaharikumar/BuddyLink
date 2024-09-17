package com.intuit.be_a_friend.services;

import com.intuit.be_a_friend.DTO.UserDTO;
import com.intuit.be_a_friend.controllers.AuthController;
import com.intuit.be_a_friend.entities.UserInfo;
import com.intuit.be_a_friend.enums.OPERATION;
import com.intuit.be_a_friend.exceptions.DuplicateUserInformationException;
import com.intuit.be_a_friend.factory.ValidatorFactory;
import com.intuit.be_a_friend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ValidatorFactory validatorFactory;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);


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
        user.setFollowersCount(0L);
        user.setFollowingCount(0L);
        userRepository.save(user);
        logger.info("User successfully created");
        return user;
    }
    public UserDTO  getUserInformation(String username) {
        UserInfo user = userRepository.findByUsername(username);
        return new UserDTO(user);
    }
}

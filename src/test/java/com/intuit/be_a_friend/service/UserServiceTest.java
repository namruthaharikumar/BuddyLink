package com.intuit.be_a_friend.service;

import com.intuit.be_a_friend.DTO.UserDTO;
import com.intuit.be_a_friend.entities.Follower;
import com.intuit.be_a_friend.entities.UserInfo;
import com.intuit.be_a_friend.exceptions.DuplicateUserInformationException;
import com.intuit.be_a_friend.factory.ValidatorFactory;
import com.intuit.be_a_friend.repositories.FollowerRepository;
import com.intuit.be_a_friend.repositories.UserRepository;
import com.intuit.be_a_friend.services.PostService;
import com.intuit.be_a_friend.services.UserService;
import com.intuit.be_a_friend.validators.UserSignupDataValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowerRepository followerRepository;

    @Mock
    private ValidatorFactory validatorFactory;

    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PostService postService;

    @Mock
    UserSignupDataValidator userSignupDataValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSignup_Success() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password1");
        userDTO.setEmail("testuser1@example.com");
        when(userRepository.existsUserInfoByEmail(userDTO.getEmail())).thenReturn(false);
        when(userRepository.existsUserInfoByUsername(userDTO.getUsername())).thenReturn(false);
        when(validatorFactory.getValidator(any())).thenReturn(userSignupDataValidator);

        UserInfo user = userService.signup(userDTO);

        assertNotNull(user);
        assertEquals(userDTO.getUsername(), user.getUsername());
        verify(userRepository, times(1)).save(any(UserInfo.class));
    }

    @Test
    void testSignup_Failure_EmailExists() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("user1");
        userDTO.setPassword("password1");
        userDTO.setEmail("user1@example.com");
        when(validatorFactory.getValidator(any())).thenReturn(userSignupDataValidator);
        when(userRepository.existsUserInfoByEmail(userDTO.getEmail())).thenReturn(true);

        assertThrows(DuplicateUserInformationException.class, () -> userService.signup(userDTO));
        verify(userRepository, times(0)).save(any(UserInfo.class));
    }

    @Test
    void testSignup_Failure_UsernameExists() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("user1");
        userDTO.setPassword("password1");
        userDTO.setEmail("user1@example.com");

        when(userRepository.existsUserInfoByEmail(userDTO.getEmail())).thenReturn(false);
        when(userRepository.existsUserInfoByUsername(userDTO.getUsername())).thenReturn(true);
        when(validatorFactory.getValidator(any())).thenReturn(userSignupDataValidator);

        assertThrows(DuplicateUserInformationException.class, () -> userService.signup(userDTO));
        verify(userRepository, times(0)).save(any(UserInfo.class));
    }

    @Test
    void testGetUserInformation_Success() {
        String username = "user1";
        UserInfo user = new UserInfo();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(user);

        UserDTO userDTO = userService.getUserInformation(username);

        assertNotNull(userDTO);
        assertEquals(username, userDTO.getUsername());
    }

    @Test
    void testGetUserInformation_UserNotFound() {
        String username = "user1";

        when(userRepository.findByUsername(username)).thenReturn(null);

        UserDTO userDTO = userService.getUserInformation(username);

        assertNull(userDTO);
    }

    @Test
    void testFollowUser_Success() {
        String userName = "user1";
        String followerUserName = "user2";

        UserInfo user = new UserInfo();
        user.setUserId("1");
        user.setUsername(userName);

        UserInfo follower = new UserInfo();
        follower.setUserId("2");
        follower.setUsername(followerUserName);

        when(userRepository.findByUsername(userName)).thenReturn(user);
        when(userRepository.findByUsername(followerUserName)).thenReturn(follower);

        boolean result = userService.followUser(userName, followerUserName);

        assertTrue(result);
        verify(followerRepository, times(1)).save(any(Follower.class));
    }

    @Test
    void testFollowUser_Failure_SameUser() {
        String userName = "user1";

        assertThrows(IllegalArgumentException.class, () -> userService.followUser(userName, userName));
    }

    @Test
    void testFollowUser_Failure_UserNotFound() {
        String userName = "user1";
        String followerUserName = "user2";

        when(userRepository.findByUsername(userName)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> userService.followUser(userName, followerUserName));
    }
}
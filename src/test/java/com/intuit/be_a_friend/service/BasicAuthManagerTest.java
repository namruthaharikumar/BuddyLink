package com.intuit.be_a_friend.service;

import com.intuit.be_a_friend.entities.UserInfo;
import com.intuit.be_a_friend.services.BasicAuthManager;
import com.intuit.be_a_friend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BasicAuthManagerTest {

    @Mock
    private UserService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BasicAuthManager basicAuthManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //Please add the comments on the use cases we are trying to test here
    @Test
    void testAuthenticate_Success() {
        String username = "testuser";
        String password = "password";
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(username);
        userInfo.setPassword("encodedPassword");

        when(userDetailsService.getUserInfo(username)).thenReturn(userInfo);
        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
        Authentication result = basicAuthManager.authenticate(authentication);

        assertNotNull(result);
        assertEquals(username, result.getName());
    }

    @Test
    void testAuthenticate_UserNotFound() {
        String username = "testuser";
        String password = "password";

        when(userDetailsService.getUserInfo(username)).thenReturn(null);

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);

        assertThrows(AuthenticationException.class, () -> {
            basicAuthManager.authenticate(authentication);
        });
    }

    @Test
    void testAuthenticate_InvalidPassword() {
        String username = "testuser";
        String password = "password";
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(username);
        userInfo.setPassword("encodedPassword");

        when(userDetailsService.getUserInfo(username)).thenReturn(userInfo);
        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(false);

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);

        assertThrows(AuthenticationException.class, () -> {
            basicAuthManager.authenticate(authentication);
        });
    }
}
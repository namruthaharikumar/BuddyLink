package com.intuit.be_a_friend.integration;

import com.intuit.be_a_friend.DTO.UserDTO;
import com.intuit.be_a_friend.enums.AccountType;
import com.intuit.be_a_friend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

class UserControllerIntegrationTest extends IntegrationTestBase {

    private static RestTemplate restTemplate = new RestTemplate();

    @LocalServerPort
    Integer serverPort;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll()
    static void setUp() {
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }
    @Test
    void testSignup_Success() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser"+"_" + LocalDateTime.now());
        userDTO.setPassword("password");
        userDTO.setEmail("testuser@example.com"+"_" + LocalDateTime.now());
        userDTO.setAccountType(AccountType.PRIVATE);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserDTO> request = new HttpEntity<>(userDTO, headers);
        String url = "http://localhost:" + serverPort + "/api/v1/user/signup";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        assertEquals(CREATED, response.getStatusCode());
        assertTrue(userRepository.findByUsername("testuser")!=null);

    }

    @Test
    void testSignup_MissingUsername() {
        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("password");
        userDTO.setEmail("testuser@example.com");
        userDTO.setAccountType(AccountType.PRIVATE);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserDTO> request = new HttpEntity<>(userDTO, headers);
        String url = "http://localhost:" + serverPort + "/api/v1/user/signup";
        try {
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            fail("Expected HttpClientErrorException");
        } catch (HttpClientErrorException e) {
            assertEquals(BAD_REQUEST, e.getStatusCode());
            assertEquals("{\"status\":\"BAD_REQUEST\",\"message\":\"Username cannot be empty\"}", e.getResponseBodyAsString());
        }
    }

    @Test
    void testLogin_Success() {
        // Assuming a user is already created in the database
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserDTO> request = new HttpEntity<>(userDTO, headers);
        String url = "http://localhost:" + serverPort + "/api/v1/user/signin";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }


    @Test
    void testLogin_InvalidCredentials() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("wrongpassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserDTO> request = new HttpEntity<>(userDTO, headers);
        String url = "http://localhost:" + serverPort + "/api/v1/user/signin";

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            fail("Expected HttpClientErrorException");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
            assertEquals("{\"status\":\"UNAUTHORIZED\",\"message\":\"Invalid username or password\"}", e.getResponseBodyAsString());
        }
    }
}
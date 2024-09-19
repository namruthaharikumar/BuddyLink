package com.intuit.be_a_friend.integration;

import com.intuit.be_a_friend.DTO.UserDTO;
import com.intuit.be_a_friend.enums.AccountType;
import com.intuit.be_a_friend.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerIntegrationTest extends IntegrationTestBase {

    private static RestTemplate restTemplate = new RestTemplate();

    @LocalServerPort
    Integer serverPort;

    @Autowired
    private UserRepository userRepository;

    static UserDTO userDTO;

    @BeforeAll()
    static void setUp() {
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        userDTO = getUserDTO();
    }
    @Test
    @Order(1)
    void testSignup_Success() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserDTO> request = new HttpEntity<>(userDTO, headers);
        String url = "http://localhost:" + serverPort + "/api/v1/user/signup";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        System.out.println("User created for "+userDTO.getUsername());
        assertEquals(CREATED, response.getStatusCode());
        assertTrue(userRepository.findByUsername(userDTO.getUsername())!=null);

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

    static UserDTO getUserDTO() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser"+"_" + LocalDateTime.now());
        userDTO.setPassword("password");
        userDTO.setAccountType(AccountType.PRIVATE);
        userDTO.setEmail("testuser"+"_" + LocalDateTime.now()+"@example.com");
        return userDTO;
    }

    @AfterAll
    @Transactional
    public void cleanUp() {
        userRepository.delete(userRepository.findByUsername(userDTO.getUsername()));
    }

}
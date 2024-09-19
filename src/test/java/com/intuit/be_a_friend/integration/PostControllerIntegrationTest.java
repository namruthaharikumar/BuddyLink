package com.intuit.be_a_friend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.be_a_friend.SocialMediaApp;
import com.intuit.be_a_friend.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SocialMediaApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @LocalServerPort
    private int port;

    private String baseUrl;
    private String validToken;
    private String invalidToken;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        validToken = jwtUtil.generateToken("user1");
        invalidToken = "eyJhbGciOiJ9.eyJzdWIiOiJBYmlzaGVrIiwiZXhwIjoxNzI2NzA1MgxLCJpYXQiOjE3MjY2NjkwODF9.bVG9sayma-j_213A32gGfAR1zQbXmFEXEttYmcZHG4Q";
        restTemplate = new RestTemplate();
    }

    @Test
    void testGetPostsByUserIds_Success() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + validToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/posts/list?page=0&size=10&sort=desc",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        System.out.println(response.getBody());
    }

    @Test
    void testGetPostsByUserIds_Unauthorized() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/api/v1/posts/list?page=0&size=10",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
        } catch (HttpClientErrorException e) {
            assertEquals(FORBIDDEN, e.getStatusCode());
        }
    }

    @Test
    void testCreatePost_Success() throws Exception {
        String postContent = "This is a test post";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + validToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(postContent, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/v1/posts/create",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Post created successfully", response.getBody());
    }

    @Test
    void testCreatePost_Unauthorized() throws Exception {
        String postContent = "This is a test post";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(postContent, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/api/v1/posts/create",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
        } catch (HttpClientErrorException e) {
            assertEquals(FORBIDDEN, e.getStatusCode());
        }

    }
}
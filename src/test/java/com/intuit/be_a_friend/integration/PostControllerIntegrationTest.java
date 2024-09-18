package com.intuit.be_a_friend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.be_a_friend.SocialMediaApp;
import com.intuit.be_a_friend.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SocialMediaApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;


    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JwtUtil jwtUtil;

    private String validToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        // Generate a valid token and an invalid token for testing
        validToken =jwtUtil.generateToken("Namrutha");
        invalidToken = "eyJhbGciOiJ9.eyJzdWIiOiJBYmlzaGVrIiwiZXhwIjoxNzI2NzA1MgxLCJpYXQiOjE3MjY2NjkwODF9.bVG9sayma-j_213A32gGfAR1zQbXmFEXEttYmcZHG4Q";
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    void testGetPostsByUserIds_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/posts/list")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                        .param("page", "0")
                        .param("size", "10").param("sort", "desc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").exists())
                .andReturn();


        String responseContent = mvcResult.getResponse().getContentAsString();
        System.out.println(responseContent);
    }

    @Test
    void testGetPostsByUserIds_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/posts/list")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreatePost_Success() throws Exception {
        String postContent = "This is a test post";

        mockMvc.perform(post("/api/v1/posts/create")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postContent)))
                .andExpect(status().isOk())
                .andExpect(content().string("Post created successfully"));
    }

    @Test
    void testCreatePost_Unauthorized() throws Exception {
        String postContent = "This is a test post";

        mockMvc.perform(post("/api/v1/posts/create")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postContent)))
                .andExpect(status().isUnauthorized());
    }
}

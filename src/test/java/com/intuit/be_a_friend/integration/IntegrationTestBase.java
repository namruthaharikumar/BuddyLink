package com.intuit.be_a_friend.integration;


import com.intuit.be_a_friend.SocialMediaApp;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = SocialMediaApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {
}

package com.intuit.be_a_friend;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching
public class SocialMediaApp  {

    public static void main(String[] args) {
        SpringApplication.run(SocialMediaApp.class, args);
    }

}

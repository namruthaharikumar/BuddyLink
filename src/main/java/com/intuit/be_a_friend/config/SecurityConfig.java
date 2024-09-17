package com.intuit.be_a_friend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;


@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Define security configurations for different endpoints
                .csrf().disable().authorizeRequests()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/user/signin/")).permitAll().
                requestMatchers(new AntPathRequestMatcher("/api/v1/user/signup")).permitAll()
                .anyRequest().authenticated();// Disable CSRF for simplicity, enable it as per your requirements



        return http.build();
    }
    @Bean
    public HandlerExceptionResolver customHandlerExceptionResolver() {
        return new ExceptionHandlerExceptionResolver();
    }
}

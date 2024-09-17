package com.intuit.be_a_friend.config;

import com.intuit.be_a_friend.controllers.UserController;
import com.intuit.be_a_friend.filters.JwtRequestFilter;
import com.intuit.be_a_friend.filters.RateLimitingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;


@Configuration
public class SecurityConfig {

    @Autowired
    RateLimitingFilter rateLimitingFilter;

    @Autowired
    JwtRequestFilter jwtRequestFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Define security configurations for different endpoints
                .csrf().disable().authorizeRequests()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/user/**")).permitAll().
                requestMatchers(new AntPathRequestMatcher("/api/v1/posts/**")).permitAll().
                anyRequest().authenticated().and()
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class).headers().frameOptions().disable();
        // Disable CSRF for simplicity, enable it as per your requirements



        return http.build();
    }
}

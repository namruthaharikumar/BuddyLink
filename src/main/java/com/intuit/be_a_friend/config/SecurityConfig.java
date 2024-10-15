package com.intuit.be_a_friend.config;

import com.intuit.be_a_friend.filters.JWTAuthenticationEntryPoint;
import com.intuit.be_a_friend.filters.JwtRequestFilter;
import com.intuit.be_a_friend.filters.RateLimitingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    RateLimitingFilter rateLimitingFilter;

    @Autowired
    @Lazy
    JwtRequestFilter jwtRequestFilter;

    @Autowired
    JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF since you are using stateless JWT authentication
                .csrf().disable()

                // Define security configurations for different endpoints
                .authorizeRequests()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/user/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/posts/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/comments/**"),new AntPathRequestMatcher("/api/v1/comments/**/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**"),new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
                .anyRequest().authenticated()
                .and()
                // Add JWT and rate-limiting filters before the UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
package com.intuit.be_a_friend.filters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JWTAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws  java.io.IOException {
        // Respond with 401 Unauthorized and a custom error message
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized - Invalid or Expired JWT");
    }
}

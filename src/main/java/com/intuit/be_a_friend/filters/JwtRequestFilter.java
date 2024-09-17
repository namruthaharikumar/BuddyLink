
package com.intuit.be_a_friend.filters;


import com.intuit.be_a_friend.DTO.UserDTO;
import com.intuit.be_a_friend.exceptions.TokenInvalidError;
import com.intuit.be_a_friend.services.UserService;
import com.intuit.be_a_friend.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

      /*  if (requestURI.equals("/api/v1/user/signin") || requestURI.equals("/api/v1/user/signup") || requestURI.equals(("/h2-console"))) {
            chain.doFilter(request, response); // Skip the JWT validation
            return;
        }*/
        chain.doFilter(request, response);

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            } catch (Exception e) {
                System.out.println("Error parsing JWT Token");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDTO userDetails = this.userDetailsService.getUserInformation(username);
            if(userDetails == null) {
                throw new ServletException("The token is invalid");
            }

            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                chain.doFilter(request, response);
            }
        }

    }
}


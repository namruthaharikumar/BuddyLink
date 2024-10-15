package com.intuit.be_a_friend.filters;

import com.intuit.be_a_friend.DTO.UserDTO;
import com.intuit.be_a_friend.services.UserService;
import com.intuit.be_a_friend.utils.Constants;
import com.intuit.be_a_friend.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
        logger.info("Processing request for URI: {}", requestURI);

        if (isAllowedEndpoint(requestURI)) {
            logger.info("Skipping JWT validation for allowed endpoint: {}", requestURI);
            chain.doFilter(request, response); // Skip the JWT validation
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                logger.info("JWT token found in the authorization header");
                username = jwtUtil.extractUsername(jwt);
                logger.info("Extracted username from JWT: {}", username);
            } else {
                logger.warn("Authorization header is missing or does not start with Bearer");
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.info("Validating token for user: {}", username);

                UserDTO userDetails = this.userDetailsService.getUserInformation(username);
                if (userDetails == null) {
                    logger.error("Invalid token for user: {}", username);
                    throw new ServletException("The token is invalid");
                }

                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                    logger.info("JWT token is valid for user: {}", username);
                    chain.doFilter(request, response);
                    return;
                } else {
                    logger.warn("JWT validation failed for user: {}", username);
                }
            }
        } catch (Exception ex) {
            request.setAttribute("message", ex.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Invalid JWT Token");
            return;
        }



        logger.error("Access forbidden for URI: {}", requestURI);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    private boolean isAllowedEndpoint(String requestURI) {
        for (String endpointPattern : Constants.allowedEndpoints) {
            Pattern pattern = Pattern.compile(endpointPattern);
            Matcher matcher = pattern.matcher(requestURI);
            if (matcher.matches()) {
                logger.info("Matched allowed endpoint pattern: {} for URI: {}", endpointPattern, requestURI);
                return true;  // The request URI matches one of the allowed patterns
            }
        }
        return false;  // No match found
    }
}
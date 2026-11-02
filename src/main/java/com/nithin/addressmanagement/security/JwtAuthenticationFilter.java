package com.nithin.addressmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// This filter intercepts EVERY incoming HTTP request.
// It checks if the request has a valid JWT token in the Authorization header.
// If yes, it tells Spring Security "this user is authenticated".
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1: Get the Authorization header
        String authHeader = request.getHeader("Authorization");

        // Step 2: Check if header exists and starts with "Bearer "
        // If not, skip this filter and move to the next one
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract the token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        try {
            // Step 4: Extract the username from the token
            String username = jwtUtil.extractUsername(token);

            // Step 5: Only authenticate if:
            //   - username was extracted successfully
            //   - no authentication already exists in SecurityContext
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Step 6: Load the full user details from the database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Step 7: Validate the token against the user
                if (jwtUtil.isTokenValid(token, userDetails)) {

                    // Step 8: Create an authentication token
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Step 9: Set the authentication in SecurityContext
                    // Now Spring Security knows this user is authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // If token is invalid or expired, do nothing.
            // The request will continue without authentication
            // and Spring Security will return 401 if the endpoint requires auth.
        }

        // Step 10: Continue with the rest of the filter chain
        filterChain.doFilter(request, response);
    }
}

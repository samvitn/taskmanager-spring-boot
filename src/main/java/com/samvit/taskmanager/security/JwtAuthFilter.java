package com.samvit.taskmanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// OncePerRequestFilter = runs exactly once per HTTP request
// This filter sits BEFORE your controllers in the request pipeline
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Step 1: Get the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Step 2: If no header or doesn't start with " Bearer ", skip this filter
        // (the request might be for a public endpoint like /api/auth/login)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);  // pass to next filter
            return;
        }

        // Step 3: Extract the token (remove "Bearer " prefix)
        final String token = authHeader.substring(7);

        // Step 4: Extract email from token
        final String email = jwtService.extractEmail(token);

        // Step 5: If email exists and user is not already authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Step 6: Load user details from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Step 7: Validate the token
            if (jwtService.isTokenValid(token)) {

                // Step 8: Create authentication token and set it in SecurityContext
                // This tells Spring Security: "this user is authenticated, let them through"
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 9: Set the authenticated user in the security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 10: Continue to the next filter (or controller if this was the last filter)
        filterChain.doFilter(request, response);
    }
}
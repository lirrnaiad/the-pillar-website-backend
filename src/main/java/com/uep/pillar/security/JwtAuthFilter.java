package com.uep.pillar.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter that processes JWT tokens from Authorization header.
 * Extends OncePerRequestFilter to ensure this filter runs once per request.
 * 
 * Performance optimization: Constructs UserDetails from JWT claims directly
 * to avoid database lookups on every authenticated request.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                String email = jwtTokenProvider.getUsernameFromToken(jwt);
                String role = jwtTokenProvider.getRoleFromToken(jwt);
                
                // Construct UserDetails from JWT claims to avoid database lookup
                // Handle null role gracefully (though tokens should always have a role)
                SimpleGrantedAuthority authority = role != null 
                    ? new SimpleGrantedAuthority("ROLE_" + role)
                    : new SimpleGrantedAuthority("ROLE_USER"); // Default fallback
                
                UserDetails userDetails = User.builder()
                    .username(email)
                    .password("") // Password not needed for token-based auth
                    .authorities(Collections.singletonList(authority))
                    .build();
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication set for user: {}", email);
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header.
     * Expected format: "Bearer <token>"
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

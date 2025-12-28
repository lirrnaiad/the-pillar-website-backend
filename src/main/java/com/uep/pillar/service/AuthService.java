package com.uep.pillar.service;

import com.uep.pillar.dto.AuthResponse;
import com.uep.pillar.model.Role;
import com.uep.pillar.model.User;
import com.uep.pillar.repository.RoleRepository;
import com.uep.pillar.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service handling login, registration, and token refresh.
 * Bridges authentication operations with JWT token generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Authenticate user with email and password, returning JWT token.
     *
     * @param email user email
     * @param password user password
     * @return authentication response with token
     * @throws BadCredentialsException if credentials are invalid
     */
    @Transactional(readOnly = true)
    public AuthResponse login(String email, String password) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );

            User user = userService.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            String token = jwtTokenProvider.generateToken(user);

            return AuthResponse.builder()
                    .token(token)
                    .user(user)
                    .expiresIn(jwtExpiration)
                    .tokenType("Bearer")
                    .build();
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.warn("Login attempt failed for email: {}", email);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Register a new user account.
     *
     * @param email user email
     * @param password user password (plain text, will be hashed)
     * @param firstName user first name
     * @param lastName user last name
     * @param roleId optional role ID (defaults to standard user if null)
     * @return authentication response with token
     */
    @Transactional
    public AuthResponse register(String email, String password, String firstName, 
                                 String lastName, Integer roleId) {
        // Determine role
        Role role = null;
        if (roleId != null) {
            role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));
        }

        // Create user (service validates email format and password strength)
        User user = userService.register(email, password, firstName, lastName, role);

        // Generate token
        String token = jwtTokenProvider.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .user(user)
                .expiresIn(jwtExpiration)
                .tokenType("Bearer")
                .build();
    }

    /**
     * Refresh an existing JWT token.
     *
     * @param token existing JWT token
     * @return authentication response with new token
     * @throws IllegalArgumentException if token is invalid or expired
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userService.findById(userId);

        String newToken = jwtTokenProvider.generateToken(user);

        return AuthResponse.builder()
                .token(newToken)
                .user(user)
                .expiresIn(jwtExpiration)
                .tokenType("Bearer")
                .build();
    }
}

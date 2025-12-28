package com.uep.pillar.resolver.mutation;

import com.uep.pillar.dto.AuthResponse;
import com.uep.pillar.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

/**
 * GraphQL Mutation Resolver for authentication operations.
 * Handles login, registration, and token refresh mutations.
 */
@Component
@RequiredArgsConstructor
public class AuthMutationResolver {

    private final AuthService authService;

    /**
     * Login with email and password.
     *
     * @param email user email
     * @param password user password
     * @return authentication response with JWT token
     */
    public AuthResponse login(String email, String password) {
        try {
            return authService.login(email, password);
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Invalid email or password");
        }
    }

    /**
     * Register a new user account.
     *
     * @param email user email
     * @param password user password
     * @param firstName user first name
     * @param lastName user last name
     * @param roleId optional role ID
     * @return authentication response with JWT token
     */
    public AuthResponse register(String email, String password, String firstName, 
                                 String lastName, String roleId) {
        try {
            Integer parsedRoleId = roleId != null ? Integer.parseInt(roleId) : null;
            return authService.register(email, password, firstName, lastName, parsedRoleId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid role ID format");
        }
    }

    /**
     * Refresh an existing JWT token.
     *
     * @param token existing JWT token
     * @return authentication response with new token
     */
    public AuthResponse refreshToken(String token) {
        try {
            return authService.refreshToken(token);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Token refresh failed: " + e.getMessage());
        }
    }
}

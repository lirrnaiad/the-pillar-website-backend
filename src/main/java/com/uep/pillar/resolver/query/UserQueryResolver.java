package com.uep.pillar.resolver.query;

import com.uep.pillar.model.User;
import com.uep.pillar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * GraphQL Query Resolver for User-related queries.
 */
@Component
@RequiredArgsConstructor
public class UserQueryResolver {

    private final UserService userService;

    /**
     * Get a user by ID.
     * 
     * @param id the user ID
     * @return the user if found, null otherwise
     */
    public User user(String id) {
        try {
            Long userId = Long.parseLong(id);
            return userService.findById(userId);
        } catch (NumberFormatException | com.uep.pillar.exception.ResourceNotFoundException e) {
            return null;
        }
    }

    /**
     * Get the current authenticated user.
     * 
     * @return the current user if authenticated, null otherwise
     */
    public User me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
            || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        // Try to get user ID from authentication principal
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            // If using UserDetails, extract email and find user
            String email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            return userService.findByEmail(email).orElse(null);
        } else if (principal instanceof String) {
            // If principal is a string (email or ID), try to find user
            String identifier = (String) principal;
            
            Long userId = null;
            try {
                userId = Long.parseLong(identifier);
            } catch (NumberFormatException e) {
                // Not a numeric ID; we'll try treating it as an email below.
            }

            if (userId != null) {
                try {
                    return userService.findById(userId);
                } catch (com.uep.pillar.exception.ResourceNotFoundException e) {
                    // No user found by ID; fall back to email lookup below.
                }
            }

            // Either identifier is not a numeric ID or user not found by ID; try email.
            return userService.findByEmail(identifier).orElse(null);
        }
        
        return null;
    }

    /**
     * Get all users.
     */
    public java.util.List<User> users() {
        return userService.findAll();
    }
}


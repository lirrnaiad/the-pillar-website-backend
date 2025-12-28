package com.uep.pillar.resolver.query;

import com.uep.pillar.model.User;
import com.uep.pillar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * GraphQL Query Resolver for User-related queries.
 */
@Component
public class UserQueryResolver {

    private final UserRepository userRepository;

    @Autowired
    public UserQueryResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get a user by ID.
     * 
     * @param id the user ID
     * @return the user if found, null otherwise
     */
    public User user(String id) {
        try {
            Long userId = Long.parseLong(id);
            return userRepository.findById(userId).orElse(null);
        } catch (NumberFormatException e) {
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

        try {
            // Try to get user ID from authentication principal
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof User) {
                return (User) principal;
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                // If using UserDetails, extract email and find user
                String email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                return userRepository.findByEmail(email).orElse(null);
            } else if (principal instanceof String) {
                // If principal is a string (email or ID), try to find user
                String identifier = (String) principal;
                try {
                    Long userId = Long.parseLong(identifier);
                    return userRepository.findById(userId).orElse(null);
                } catch (NumberFormatException e) {
                    return userRepository.findByEmail(identifier).orElse(null);
                }
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}


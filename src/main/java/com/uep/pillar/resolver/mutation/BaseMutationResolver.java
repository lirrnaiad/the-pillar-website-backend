package com.uep.pillar.resolver.mutation;

import com.uep.pillar.model.User;
import com.uep.pillar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Base class for GraphQL mutation resolvers.
 * Provides common helper methods for ID parsing and authentication.
 */
@RequiredArgsConstructor
public abstract class BaseMutationResolver {

    protected final UserService userService;

    /**
     * Get current authenticated user from SecurityContext.
     * Handles both User entity and UserDetails principals (from JWT authentication).
     *
     * @return the authenticated user, or null if not authenticated
     */
    protected User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
            || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        // If principal is already a User entity, return it directly
        if (principal instanceof User) {
            return (User) principal;
        }
        
        // If principal is UserDetails (from JWT filter), extract email and fetch User entity
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userService.findByEmail(email).orElse(null);
        }
        
        return null;
    }

    /**
     * Parse Long ID from GraphQL ID string.
     *
     * @param id the string ID to parse
     * @param fieldName the name of the field (for error messages)
     * @return the parsed Long ID
     * @throws IllegalArgumentException if the ID format is invalid
     */
    protected Long parseLongId(String id, String fieldName) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }

    /**
     * Parse Integer ID from GraphQL ID string.
     *
     * @param id the string ID to parse
     * @param fieldName the name of the field (for error messages)
     * @return the parsed Integer ID
     * @throws IllegalArgumentException if the ID format is invalid
     */
    protected Integer parseIntegerId(String id, String fieldName) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }
}

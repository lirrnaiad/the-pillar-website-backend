package com.uep.pillar.resolver.mutation;

import com.uep.pillar.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Base class for GraphQL mutation resolvers.
 * Provides common helper methods for ID parsing and authentication.
 */
public abstract class BaseMutationResolver {

    /**
     * Get current authenticated user from SecurityContext.
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
        
        if (principal instanceof User) {
            return (User) principal;
        }
        
        // Could extend to handle UserDetails extraction if needed
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

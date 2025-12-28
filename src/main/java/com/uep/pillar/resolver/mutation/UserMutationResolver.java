package com.uep.pillar.resolver.mutation;

import com.uep.pillar.dto.CreateUserInput;
import com.uep.pillar.dto.UpdateUserInput;
import com.uep.pillar.model.Role;
import com.uep.pillar.model.User;
import com.uep.pillar.repository.RoleRepository;
import com.uep.pillar.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * GraphQL Mutation Resolver for User mutations.
 * Handles user creation, updates, deletion, and password changes.
 */
@Component
public class UserMutationResolver extends BaseMutationResolver {

    private final RoleRepository roleRepository;

    // UserService is required by BaseMutationResolver for getCurrentUser()
    public UserMutationResolver(UserService userService, RoleRepository roleRepository) {
        super(userService);
        this.roleRepository = roleRepository;
    }

    /**
     * Create a new user.
     *
     * @param input user creation input
     * @return the created user
     */
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(CreateUserInput input) {
        // Fetch role if provided
        Role role = null;
        if (input.getRoleId() != null) {
            role = roleRepository.findById(input.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + input.getRoleId()));
        }

        // Build user with raw password (service will hash it)
        User user = User.builder()
            .email(input.getEmail())
            .password(input.getPassword())
            .firstName(input.getFirstName())
            .lastName(input.getLastName())
            .avatarUrl(input.getAvatarUrl())
            .bio(input.getBio())
            .role(role)
            .build();

        return this.userService.create(user);
    }

    /**
     * Update an existing user.
     *
     * @param input user update input
     * @return the updated user
     */
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(UpdateUserInput input) {
        Long id = parseLongId(input.getId(), "User ID");

        // Fetch role if provided
        Role role = null;
        if (input.getRoleId() != null) {
            role = roleRepository.findById(input.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + input.getRoleId()));
        }

        // Use updateWithEmail if email is changing
        return this.userService.updateWithEmail(
            id,
            input.getEmail(),
            input.getFirstName(),
            input.getLastName(),
            input.getAvatarUrl(),
            input.getBio(),
            role
        );
    }

    /**
     * Delete a user (soft delete).
     *
     * @param id the user ID
     * @return true on success
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteUser(String id) {
        Long userId = parseLongId(id, "User ID");
        this.userService.softDelete(userId);
        return true;
    }

    /**
     * Change user password.
     *
     * @param id the user ID
     * @param oldPassword the current password (for validation)
     * @param newPassword the new password
     * @return true on success
     */
    public Boolean changePassword(String id, String oldPassword, String newPassword) {
        Long userId = parseLongId(id, "User ID");
        
        // Validate old password
        User user = this.userService.findById(userId);
        if (!this.userService.validatePassword(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Change to new password
        this.userService.changePassword(userId, newPassword);
        return true;
    }
}

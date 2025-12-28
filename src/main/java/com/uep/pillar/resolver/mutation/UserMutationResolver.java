package com.uep.pillar.resolver.mutation;

import com.uep.pillar.dto.CreateUserInput;
import com.uep.pillar.dto.UpdateUserInput;
import com.uep.pillar.model.Role;
import com.uep.pillar.model.User;
import com.uep.pillar.repository.RoleRepository;
import com.uep.pillar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * GraphQL Mutation Resolver for User mutations.
 * Handles user creation, updates, deletion, and password changes.
 */
@Component
@RequiredArgsConstructor
public class UserMutationResolver extends BaseMutationResolver {

    private final UserService userService;
    private final RoleRepository roleRepository;

    /**
     * Create a new user.
     *
     * @param input user creation input
     * @return the created user
     */
    public User createUser(CreateUserInput input) {
        // Fetch role if provided
        Role role = null;
        if (input.getRoleId() != null) {
            Integer roleId = parseIntegerId(input.getRoleId(), "Role ID");
            role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));
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

        return userService.create(user);
    }

    /**
     * Update an existing user.
     *
     * @param input user update input
     * @return the updated user
     */
    public User updateUser(UpdateUserInput input) {
        Long id = parseLongId(input.getId(), "User ID");

        // Fetch role if provided
        Role role = null;
        if (input.getRoleId() != null) {
            Integer roleId = parseIntegerId(input.getRoleId(), "Role ID");
            role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));
        }

        // Use updateWithEmail if email is changing
        return userService.updateWithEmail(
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
    public Boolean deleteUser(String id) {
        Long userId = parseLongId(id, "User ID");
        userService.softDelete(userId);
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
        User user = userService.findById(userId);
        if (!userService.validatePassword(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Change to new password
        userService.changePassword(userId, newPassword);
        return true;
    }
}

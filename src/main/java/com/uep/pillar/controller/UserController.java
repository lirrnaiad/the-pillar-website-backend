package com.uep.pillar.controller;

import com.uep.pillar.dto.CreateUserInput;
import com.uep.pillar.dto.UpdateUserInput;
import com.uep.pillar.model.Role;
import com.uep.pillar.model.User;
import com.uep.pillar.repository.RoleRepository;
import com.uep.pillar.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user management operations.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    @Data
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
    }

    /**
     * Get all users.
     * GET /api/users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    /**
     * Get user by ID.
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    /**
     * Get current authenticated user.
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
            || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return ResponseEntity.ok((User) principal);
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            return userService.findByEmail(email)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } else if (principal instanceof String) {
            String identifier = (String) principal;
            try {
                Long userId = Long.parseLong(identifier);
                return ResponseEntity.ok(userService.findById(userId));
            } catch (NumberFormatException e) {
                return userService.findByEmail(identifier)
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
            }
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Create a new user.
     * POST /api/users
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody CreateUserInput input) {
        Role role = null;
        if (input.getRoleId() != null) {
            role = roleRepository.findById(input.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + input.getRoleId()));
        }

        User user = User.builder()
                .email(input.getEmail())
                .password(input.getPassword())
                .firstName(input.getFirstName())
                .lastName(input.getLastName())
                .avatarUrl(input.getAvatarUrl())
                .bio(input.getBio())
                .role(role)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(user));
    }

    /**
     * Update an existing user.
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UpdateUserInput input) {
        Role role = null;
        if (input.getRoleId() != null) {
            role = roleRepository.findById(input.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + input.getRoleId()));
        }

        User updated = userService.updateWithEmail(
                id,
                input.getEmail(),
                input.getFirstName(),
                input.getLastName(),
                input.getAvatarUrl(),
                input.getBio(),
                role
        );

        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a user (soft delete).
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Change user password.
     * POST /api/users/{id}/change-password
     */
    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request) {
        User user = userService.findById(id);
        if (!userService.validatePassword(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        userService.changePassword(id, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}


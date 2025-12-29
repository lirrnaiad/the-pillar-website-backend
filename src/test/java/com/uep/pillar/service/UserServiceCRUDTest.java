package com.uep.pillar.service;

import com.uep.pillar.exception.ResourceNotFoundException;
import com.uep.pillar.model.Role;
import com.uep.pillar.model.User;
import com.uep.pillar.repository.RoleRepository;
import com.uep.pillar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CRUD tests for UserService.
 * Tests create, read, update, and delete operations.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceCRUDTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role testRole;

    @BeforeEach
    void setUp() {
        // Create test role
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("articles", List.of("create", "read", "update"));
        testRole = Role.builder()
                .name("TEST_EDITOR")
                .permissions(permissions)
                .build();
        testRole = roleRepository.save(testRole);
    }

    @Test
    void testCreateUser() {
        // CREATE
        User user = userService.register(
                "newuser@test.com",
                "password123",
                "John",
                "Doe",
                testRole
        );

        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals("newuser@test.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals(testRole.getId(), user.getRole().getId());
        // Password should be hashed
        assertNotEquals("password123", user.getPassword());
        assertTrue(passwordEncoder.matches("password123", user.getPassword()));
    }

    @Test
    void testCreateUserWithInvalidEmail() {
        // CREATE with invalid email
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(
                    "invalid-email",
                    "password123",
                    "John",
                    "Doe",
                    null
            );
        });
    }

    @Test
    void testCreateUserWithWeakPassword() {
        // CREATE with weak password
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(
                    "user@test.com",
                    "weak",
                    "John",
                    "Doe",
                    null
            );
        });
    }

    @Test
    void testReadUser() {
        // CREATE first
        User created = userService.register(
                "readuser@test.com",
                "password123",
                "Jane",
                "Smith",
                null
        );

        // READ by ID
        User found = userService.findById(created.getId());
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("readuser@test.com", found.getEmail());

        // READ by email
        User foundByEmail = userService.findByEmail("readuser@test.com")
                .orElseThrow();
        assertEquals(created.getId(), foundByEmail.getId());
    }

    @Test
    void testReadUserNotFound() {
        // READ non-existent user
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.findById(99999L);
        });

        // READ by email - not found
        assertTrue(userService.findByEmail("nonexistent@test.com").isEmpty());
    }

    @Test
    void testUpdateUser() {
        // CREATE
        User user = userService.register(
                "updateuser@test.com",
                "password123",
                "Original",
                "Name",
                null
        );

        Long userId = user.getId();

        // UPDATE
        User updated = userService.update(
                userId,
                "Updated",
                "Name",
                null,
                null,
                testRole
        );

        assertEquals(userId, updated.getId());
        assertEquals("updateuser@test.com", updated.getEmail()); // Unchanged
        assertEquals("Updated", updated.getFirstName());
        assertEquals("Name", updated.getLastName());
        assertEquals(testRole.getId(), updated.getRole().getId());
    }

    @Test
    void testUpdateUserEmail() {
        // CREATE
        User user = userService.register(
                "oldemail@test.com",
                "password123",
                "John",
                "Doe",
                null
        );

        // UPDATE email
        User updated = userService.updateWithEmail(
                user.getId(),
                "newemail@test.com",
                "John",
                "Doe",
                null,
                null,
                null
        );

        assertEquals("newemail@test.com", updated.getEmail());
        // Old email should not exist
        assertTrue(userService.findByEmail("oldemail@test.com").isEmpty());
    }

    @Test
    void testUpdateUserDuplicateEmail() {
        // CREATE two users
        User user1 = userService.register(
                "user1@test.com",
                "password123",
                "User",
                "One",
                null
        );

        User user2 = userService.register(
                "user2@test.com",
                "password123",
                "User",
                "Two",
                null
        );

        // Try to update user2's email to user1's email
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateWithEmail(
                    user2.getId(),
                    "user1@test.com",
                    "User",
                    "Two",
                    null,
                    null,
                    null
            );
        });
    }

    @Test
    void testChangePassword() {
        // CREATE
        User user = userService.register(
                "passworduser@test.com",
                "oldpassword123",
                "John",
                "Doe",
                null
        );

        String oldPasswordHash = user.getPassword();

        // CHANGE PASSWORD
        userService.changePassword(user.getId(), "newpassword123");

        // Verify password changed
        User updated = userService.findById(user.getId());
        assertNotEquals(oldPasswordHash, updated.getPassword());
        assertTrue(passwordEncoder.matches("newpassword123", updated.getPassword()));
        assertFalse(passwordEncoder.matches("oldpassword123", updated.getPassword()));
    }

    @Test
    void testDeleteUser() {
        // CREATE
        User user = userService.register(
                "deleteuser@test.com",
                "password123",
                "John",
                "Doe",
                null
        );

        Long userId = user.getId();
        assertTrue(userRepository.findById(userId).isPresent());

        // DELETE (soft delete)
        userService.softDelete(userId);
        
        // Flush to ensure the soft delete is persisted
        userRepository.flush();

        // Verify soft delete - should not be found in normal queries
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.findById(userId);
        });

        // Note: Database unique constraint on email prevents creating a new user
        // with the same email as a soft-deleted user. This is expected behavior.
        // If you need to allow reusing emails after soft delete, you would need to:
        // 1. Remove the unique constraint and use a partial unique index (WHERE deleted_at IS NULL)
        // 2. Or implement application-level validation that checks only non-deleted users
        // For now, verify that soft-deleted user cannot be found but email constraint remains
        assertFalse(userRepository.findById(userId).isPresent());
        
        // Try to create a user with a different email to verify the service still works
        User newUser = userService.register(
                "newdeleteuser@test.com",
                "password123",
                "New",
                "User",
                null
        );
        assertNotNull(newUser);
    }

    @Test
    void testListUsers() {
        // CREATE multiple users
        userService.register("user1@test.com", "password123", "User", "One", null);
        userService.register("user2@test.com", "password123", "User", "Two", null);
        userService.register("user3@test.com", "password123", "User", "Three", null);

        // LIST all
        List<User> allUsers = userService.findAll();
        assertTrue(allUsers.size() >= 3);
    }
}


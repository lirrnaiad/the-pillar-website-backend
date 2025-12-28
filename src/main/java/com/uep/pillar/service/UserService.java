package com.uep.pillar.service;

import com.uep.pillar.exception.ResourceNotFoundException;
import com.uep.pillar.model.Role;
import com.uep.pillar.model.User;
import com.uep.pillar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^(?=.{1,64}@)(?!.*\\.\\.)[A-Za-z0-9](?:[A-Za-z0-9._%+-]*[A-Za-z0-9])?@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User register(String email, String rawPassword, String firstName, String lastName, Role role) {
        User user = User.builder()
                .email(email)
                .password(rawPassword)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .build();
        return createUser(user);
    }

    @Transactional
    public User create(User userWithRawPassword) {
        return createUser(userWithRawPassword);
    }

    /**
     * Common method to create a user with validation and password encoding.
     */
    private User createUser(User userWithRawPassword) {
        validateEmailFormat(userWithRawPassword.getEmail());
        validateEmailUniqueness(userWithRawPassword.getEmail(), null);
        validatePasswordStrength(userWithRawPassword.getPassword());
        String hashed = passwordEncoder.encode(userWithRawPassword.getPassword());
        userWithRawPassword.setPassword(hashed);
        return userRepository.save(userWithRawPassword);
    }

    @Transactional
    public User update(Long id, String firstName, String lastName, String avatarUrl, String bio, Role role) {
        User existing = findById(id);
        if (firstName != null) existing.setFirstName(firstName);
        if (lastName != null) existing.setLastName(lastName);
        if (avatarUrl != null) existing.setAvatarUrl(avatarUrl);
        if (bio != null) existing.setBio(bio);
        if (role != null) existing.setRole(role);
        return userRepository.save(existing);
    }

    /**
     * Update user including email change.
     * Validates email format and uniqueness if email is being changed.
     */
    @Transactional
    public User updateWithEmail(Long id, String email, String firstName, String lastName, 
                                String avatarUrl, String bio, Role role) {
        User existing = findById(id);
        if (email != null && !email.equals(existing.getEmail())) {
            validateEmailFormat(email);
            validateEmailUniqueness(email, id);
            existing.setEmail(email);
        }
        if (firstName != null) existing.setFirstName(firstName);
        if (lastName != null) existing.setLastName(lastName);
        if (avatarUrl != null) existing.setAvatarUrl(avatarUrl);
        if (bio != null) existing.setBio(bio);
        if (role != null) existing.setRole(role);
        return userRepository.save(existing);
    }

    @Transactional
    public void changePassword(Long id, String rawPassword) {
        User existing = findById(id);
        validatePasswordStrength(rawPassword);
        existing.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(existing);
    }

    @Transactional
    public void softDelete(Long id) {
        userRepository.softDelete(id, LocalDateTime.now());
    }

    @Transactional
    public void restore(Long id) {
        userRepository.restore(id);
    }

    public boolean validatePassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    /**
     * Validate email format only.
     */
    private void validateEmailFormat(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    /**
     * Validate password strength requirements.
     * Ensures minimum length and basic complexity.
     */
    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }
        // Additional complexity checks can be added here
        if (!password.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
    }

    /**
     * Validate email uniqueness, excluding the given user ID (for updates).
     * 
     * @param email the email to check
     * @param excludeUserId optional user ID to exclude from uniqueness check
     */
    private void validateEmailUniqueness(String email, Long excludeUserId) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            // If excludeUserId is provided and matches, it's the same user - no conflict
            if (excludeUserId != null && existingUser.get().getId() != null 
                && existingUser.get().getId().equals(excludeUserId)) {
                return; // Same user, no conflict
            }
            throw new IllegalArgumentException("Email already in use");
        }
    }
}

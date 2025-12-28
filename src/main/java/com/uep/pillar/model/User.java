package com.uep.pillar.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a system user (admin, editor, writer).
 * Supports soft delete via deletedAt timestamp.
 * 
 * Note: Queries automatically exclude soft-deleted users via @SQLRestriction.
 * To include deleted users, use native queries or remove the filter.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_deleted_at", columnList = "deleted_at")
})
@SQLRestriction("deleted_at IS NULL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Stores the user's password hash.
     * <p>
     * IMPORTANT: This field must never contain a plain text password.
     * Passwords must be hashed in the service/security layer before being
     * assigned to this field (BCrypt recommended).
     * <p>
     * WARNING: Never include this field in toString() or expose via API responses.
     * The @Setter is kept for flexibility, but password hashing MUST be done
     * at the service layer before calling setPassword().
     */
    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Soft delete timestamp. If not null, the user is considered deleted.
     * Filtered automatically by @SQLRestriction.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Check if the user is soft-deleted.
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Get the user's full name.
     * <p>
     * This method is null-safe: if one name part is null, returns the other;
     * if both are null, returns an empty string.
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "";
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Note: Intentionally excludes password field for security.
     */
    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "'}";
    }
}

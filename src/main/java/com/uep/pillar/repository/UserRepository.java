package com.uep.pillar.repository;

import com.uep.pillar.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity operations.
 * 
 * Note: Due to @SQLRestriction on User entity, all queries automatically
 * exclude soft-deleted users (where deleted_at IS NOT NULL).
 * Use native queries to include deleted users if needed.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email address.
     * 
     * @param email the user's email
     * @return the user if found (excludes soft-deleted)
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email exists.
     * 
     * @param email the email to check
     * @return true if exists (excludes soft-deleted)
     */
    boolean existsByEmail(String email);

    /**
     * Find all users with a specific role.
     * 
     * @param roleId the role ID
     * @param pageable pagination info
     * @return paginated list of users
     */
    Page<User> findByRoleId(Integer roleId, Pageable pageable);

    /**
     * Find all users with a specific role name.
     * 
     * @param roleName the role name
     * @return list of users with that role
     */
    @Query("SELECT u FROM User u WHERE u.role.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    /**
     * Soft delete a user by setting deleted_at timestamp.
     * 
     * @param userId the user ID to delete
     * @param deletedAt the deletion timestamp
     */
    @Modifying
    @Query("UPDATE User u SET u.deletedAt = :deletedAt WHERE u.id = :userId")
    void softDelete(@Param("userId") Long userId, @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * Restore a soft-deleted user.
     * Uses native query to bypass @SQLRestriction filter.
     * 
     * @param userId the user ID to restore
     */
    @Modifying
    @Query(value = "UPDATE users SET deleted_at = NULL WHERE id = :userId", nativeQuery = true)
    void restore(@Param("userId") Long userId);

    /**
     * Find a user by ID including soft-deleted users.
     * Uses native query to bypass @SQLRestriction filter.
     * 
     * @param userId the user ID
     * @return the user if found (includes soft-deleted)
     */
    @Query(value = "SELECT * FROM users WHERE id = :userId", nativeQuery = true)
    Optional<User> findByIdIncludingDeleted(@Param("userId") Long userId);

    /**
     * Find a user by email including soft-deleted users.
     * Uses native query to bypass @SQLRestriction filter.
     * 
     * @param email the user's email
     * @return the user if found (includes soft-deleted)
     */
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailIncludingDeleted(@Param("email") String email);
}


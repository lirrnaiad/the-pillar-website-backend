package com.uep.pillar.repository;

import com.uep.pillar.model.AuditLog;
import com.uep.pillar.model.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditLog entity operations.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs for a specific entity.
     * 
     * @param entityType the entity type (e.g., "ARTICLE", "USER")
     * @param entityId the entity ID
     * @return list of audit logs, ordered by created_at descending
     */
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.createdAt DESC")
    List<AuditLog> findByEntityTypeAndEntityId(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    /**
     * Find audit logs for a specific entity with pagination.
     * 
     * @param entityType the entity type
     * @param entityId the entity ID
     * @param pageable pagination info
     * @return paginated list of audit logs
     */
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId, Pageable pageable);

    /**
     * Find audit logs by user.
     * 
     * @param userId the user ID who performed the actions
     * @param pageable pagination info
     * @return paginated list of audit logs
     */
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find audit logs by action type.
     * 
     * @param action the action type
     * @param pageable pagination info
     * @return paginated list of audit logs
     */
    Page<AuditLog> findByActionOrderByCreatedAtDesc(AuditAction action, Pageable pageable);

    /**
     * Find audit logs by entity type.
     * 
     * @param entityType the entity type
     * @param pageable pagination info
     * @return paginated list of audit logs
     */
    Page<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType, Pageable pageable);

    /**
     * Find audit logs within a date range.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination info
     * @return paginated list of audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        Pageable pageable
    );

    /**
     * Find recent audit logs.
     * 
     * @param pageable pagination info (use PageRequest.of(0, limit) to limit results)
     * @return page of recent audit logs
     */
    @Query("SELECT a FROM AuditLog a ORDER BY a.createdAt DESC")
    Page<AuditLog> findRecent(Pageable pageable);

    /**
     * Find login/logout audit logs for a user.
     * 
     * @param userId the user ID
     * @param pageable pagination info
     * @return paginated list of auth-related audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.action IN (com.uep.pillar.model.enums.AuditAction.LOGIN, com.uep.pillar.model.enums.AuditAction.LOGOUT) ORDER BY a.createdAt DESC")
    Page<AuditLog> findAuthLogsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Count audit logs by action type within a date range.
     * Useful for generating activity reports.
     * 
     * @param action the action type
     * @param startDate the start date
     * @param endDate the end date
     * @return count of matching logs
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = :action AND a.createdAt BETWEEN :startDate AND :endDate")
    long countByActionAndDateRange(
        @Param("action") AuditAction action,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Delete audit logs older than a specified date.
     * Useful for log retention policies.
     * 
     * @param cutoffDate delete logs created before this date
     */
    @Modifying
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}


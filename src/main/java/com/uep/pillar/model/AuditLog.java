package com.uep.pillar.model;

import com.uep.pillar.model.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Entity representing an audit log entry for tracking CMS actions.
 * Used for accountability and debugging.
 * 
 * <h3>JSONB Fields (oldValue, newValue)</h3>
 * These fields use Map&lt;String, Object&gt; to accommodate different entity types:
 * <pre>
 * // Article audit example:
 * {
 *   "id": 123,
 *   "title": "Article Title",
 *   "status": "PUBLISHED",
 *   "authorId": 1
 * }
 * 
 * // User audit example:
 * {
 *   "id": 1,
 *   "email": "user@example.com",
 *   "firstName": "John",
 *   "roleId": 2
 * }
 * </pre>
 * 
 * The flexible structure allows auditing any entity type without schema changes.
 * Sensitive fields (like passwords) should never be included in audit logs.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_logs_user", columnList = "user_id"),
    @Index(name = "idx_audit_logs_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who performed the action.
     * May be null for system-generated actions.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Type of action performed.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    /**
     * Type of entity affected (ARTICLE, MEDIA, USER, CATEGORY, TAG, etc.).
     */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /**
     * ID of the affected entity.
     */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * Previous state of the entity (JSON).
     * 
     * Structure varies by entity type. Never include sensitive data like passwords.
     * @see AuditLog class documentation for structure examples.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private Map<String, Object> oldValue;

    /**
     * New state of the entity (JSON).
     * 
     * Structure varies by entity type. Never include sensitive data like passwords.
     * @see AuditLog class documentation for structure examples.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private Map<String, Object> newValue;

    /**
     * IP address of the user (IPv4 or IPv6).
     * <p>
     * Only syntactically valid IP addresses should be stored here:
     * <ul>
     *     <li>IPv4 in dotted-decimal notation, for example {@code 203.0.113.42}</li>
     *     <li>IPv6 in standard text representation, for example {@code 2001:db8::1},
     *     validated according to RFC&nbsp;5952 (canonical textual representation)</li>
     * </ul>
     * Validation of IP address format must be performed at the service layer using
     * a standards-compliant IP address parser/validator.
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent string of the client.
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return id != null && Objects.equals(id, auditLog.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "AuditLog{id=" + id + ", action=" + action + ", entityType='" + entityType + "', entityId=" + entityId + "}";
    }
}

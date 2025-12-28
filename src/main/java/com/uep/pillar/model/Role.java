package com.uep.pillar.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Entity representing a user role with associated permissions.
 * Permissions are stored as JSONB in PostgreSQL.
 * 
 * Note: Uses Integer for ID as roles are a small lookup table (SERIAL in PostgreSQL).
 * 
 * <h3>Permissions Structure (JSONB)</h3>
 * <pre>
 * {
 *   "all": true,  // Admin: full access
 *   // OR specific permissions:
 *   "articles": ["create", "read", "update", "delete", "publish"],
 *   "media": ["create", "read", "delete"],
 *   "users": ["read"]
 * }
 * </pre>
 * 
 * The flexible Map&lt;String, Object&gt; allows for evolving permission structures
 * without schema changes. Type safety is enforced at the service layer.
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Permissions stored as JSON object.
     * 
     * Expected structure:
     * - "all": Boolean - if true, grants all permissions (admin)
     * - "articles": List&lt;String&gt; - article permissions
     * - "media": List&lt;String&gt; - media permissions
     * - "users": List&lt;String&gt; - user management permissions
     * 
     * Type safety is enforced at the service layer when checking permissions.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> permissions;

    /**
     * Check if this role has a specific permission.
     * 
     * @param resource The resource (e.g., "articles", "media")
     * @param action The action (e.g., "create", "read", "update", "delete")
     * @return true if permission granted
     */
    public boolean hasPermission(String resource, String action) {
        if (permissions == null) return false;
        
        // Admin has all permissions
        Object allPerm = permissions.get("all");
        if (allPerm instanceof Boolean && (Boolean) allPerm) {
            return true;
        }
        
        Object resourcePerms = permissions.get(resource);
        if (resourcePerms instanceof List<?> permList) {
            try {
                return permList.stream()
                    .filter(p -> p instanceof String)
                    .map(p -> (String) p)
                    .anyMatch(p -> p.equals(action));
            } catch (Exception e) {
                // Malformed permission data - log and return false
                log.warn("Failed to check permission for resource '{}' and action '{}' in role '{}': {}", 
                         resource, action, name, e.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return id != null && Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Role{id=" + id + ", name='" + name + "'}";
    }
}

package com.uep.pillar.resolver.query;

import com.uep.pillar.model.AuditLog;
import com.uep.pillar.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GraphQL Query Resolver for AuditLog queries.
 */
@Component
@RequiredArgsConstructor
public class AuditLogQueryResolver {

    private final AuditLogService auditLogService;

    /**
     * Get audit logs optionally filtered by entity type and ID.
     */
    public List<AuditLog> auditLogs(String entityType, String entityId) {
        Long parsedId = null;
        if (entityId != null) {
            try {
                parsedId = Long.parseLong(entityId);
            } catch (NumberFormatException e) {
                return List.of();
            }
        }
        return auditLogService.findAuditLogs(entityType, parsedId);
    }
}

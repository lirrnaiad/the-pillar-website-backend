package com.uep.pillar.controller;

import com.uep.pillar.model.AuditLog;
import com.uep.pillar.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for audit log operations.
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Get audit logs with optional filters.
     * GET /api/audit-logs
     */
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId) {
        List<AuditLog> logs = auditLogService.findAuditLogs(entityType, entityId);
        return ResponseEntity.ok(logs);
    }
}


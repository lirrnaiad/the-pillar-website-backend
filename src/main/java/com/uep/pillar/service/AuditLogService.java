package com.uep.pillar.service;

import com.uep.pillar.model.AuditLog;
import com.uep.pillar.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<AuditLog> findAuditLogs(String entityType, Long entityId) {
        if (entityType != null && entityId != null) {
            return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
        }
        if (entityType != null) {
            return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(
                    entityType,
                    PageRequest.of(0, 100)
            ).getContent();
        }
        return auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}

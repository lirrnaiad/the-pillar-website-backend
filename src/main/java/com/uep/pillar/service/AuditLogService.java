package com.uep.pillar.service;

import com.uep.pillar.model.Article;
import com.uep.pillar.model.AuditLog;
import com.uep.pillar.model.User;
import com.uep.pillar.model.enums.AuditAction;
import com.uep.pillar.repository.AuditLogRepository;
import com.uep.pillar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

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

    /**
     * Log a generic CMS action.
     */
    @Transactional
    public void log(String entityType, Long entityId, AuditAction action, Map<String, Object> oldValue,
                    Map<String, Object> newValue, String note) {
        User currentUser = resolveCurrentUser();
        HttpServletRequest req = resolveCurrentRequest();
        String ip = req != null ? extractClientIp(req) : null;
        String ua = req != null ? req.getHeader("User-Agent") : null;

        AuditLog log = AuditLog.builder()
                .user(currentUser)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(ip)
                .userAgent(ua)
                .note(note)
                .build();
        auditLogRepository.save(log);
    }

    /**
     * Convenience method for article actions.
     */
    @Transactional
    public void logArticle(AuditAction action, Article before, Article after, String note) {
        Map<String, Object> oldMap = before != null ? toMap(before) : null;
        Map<String, Object> newMap = after != null ? toMap(after) : null;
        Long entityId = (after != null && after.getId() != null) ? after.getId() : (before != null ? before.getId() : null);
        log("ARTICLE", entityId, action, oldMap, newMap, note);
    }

    private Map<String, Object> toMap(Article a) {
        Map<String, Object> m = new HashMap<>();
        if (a == null) return m;
        m.put("id", a.getId());
        m.put("title", a.getTitle());
        m.put("slug", a.getSlug());
        m.put("status", a.getStatus() != null ? a.getStatus().name() : null);
        m.put("authorId", a.getAuthor() != null ? a.getAuthor().getId() : null);
        m.put("categoryId", a.getCategory() != null ? a.getCategory().getId() : null);
        m.put("issueId", a.getIssue() != null ? a.getIssue().getId() : null);
        m.put("featured", a.isFeatured());
        m.put("viewCount", a.getViewCount());
        return m;
    }

    private User resolveCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            String email = auth.getName();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception ex) {
            return null;
        }
    }

    private HttpServletRequest resolveCurrentRequest() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                return sra.getRequest();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // first IP in the list
            int comma = xff.indexOf(',');
            return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
        }
        String xrip = request.getHeader("X-Real-IP");
        if (xrip != null && !xrip.isBlank()) return xrip.trim();
        return request.getRemoteAddr();
    }
}

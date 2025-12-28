package com.uep.pillar.resolver.mutation;

import com.uep.pillar.model.Article;
import com.uep.pillar.model.enums.AuditAction;
import com.uep.pillar.service.ArticleRevisionService;
import com.uep.pillar.service.AuditLogService;
import com.uep.pillar.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * GraphQL Mutation Resolver for ArticleRevision operations.
 */
@Component
public class ArticleRevisionMutationResolver extends BaseMutationResolver {

    private final ArticleRevisionService articleRevisionService;
    private final AuditLogService auditLogService;

    public ArticleRevisionMutationResolver(ArticleRevisionService articleRevisionService,
                                           AuditLogService auditLogService,
                                           UserService userService) {
        super(userService);
        this.articleRevisionService = articleRevisionService;
        this.auditLogService = auditLogService;
    }

    /**
     * Restore article content/title from a specific revision.
     * @param revisionId the revision ID
     * @return the updated article
     */
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public Article restoreArticleFromRevision(String revisionId) {
        Long id = parseLongId(revisionId, "Revision ID");
        Article updated = articleRevisionService.restoreFromRevision(id);
        auditLogService.logArticle(AuditAction.RESTORE, null, updated, "restore-from-revision");
        return updated;
    }
}

package com.uep.pillar.controller;

import com.uep.pillar.model.Article;
import com.uep.pillar.model.ArticleRevision;
import com.uep.pillar.model.User;
import com.uep.pillar.service.ArticleRevisionService;
import com.uep.pillar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for article revision operations.
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleRevisionController {

    private final ArticleRevisionService articleRevisionService;
    private final UserService userService;

    /**
     * Get revision history for an article.
     * GET /api/articles/{articleId}/revisions
     */
    @GetMapping("/{articleId}/revisions")
    public ResponseEntity<List<ArticleRevision>> getArticleRevisions(@PathVariable Long articleId) {
        List<ArticleRevision> revisions = articleRevisionService.findByArticleId(articleId);
        return ResponseEntity.ok(revisions);
    }

    /**
     * Restore an article's content/title from a specific revision.
     * POST /api/articles/revisions/{revisionId}/restore
     */
    @PostMapping("/revisions/{revisionId}/restore")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public ResponseEntity<Article> restoreArticleFromRevision(@PathVariable Long revisionId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Authentication required to restore revision");
        }
        Article article = articleRevisionService.restoreFromRevision(revisionId, currentUser);
        return ResponseEntity.ok(article);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
            || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        }
        
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userService.findByEmail(email).orElse(null);
        }
        
        return null;
    }
}


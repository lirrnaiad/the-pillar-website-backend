package com.uep.pillar.service;

import com.uep.pillar.exception.ResourceNotFoundException;
import com.uep.pillar.model.Article;
import com.uep.pillar.model.ArticleRevision;
import com.uep.pillar.model.User;
import com.uep.pillar.repository.ArticleRevisionRepository;
import com.uep.pillar.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleRevisionService {

    private final ArticleRevisionRepository articleRevisionRepository;
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public List<ArticleRevision> findByArticleId(Long articleId) {
        return articleRevisionRepository.findByArticleId(articleId);
    }

    /**
     * Save a snapshot of the current article state before an update.
     * Only title and content are versioned for now.
     */
    @Transactional
    public ArticleRevision saveSnapshot(Article article, User revisedBy, String note) {
        ArticleRevision revision = ArticleRevision.builder()
                .article(article)
                .title(article.getTitle())
                .content(article.getContent())
                .revisedBy(revisedBy)
                .revisionNote(note)
                .build();
        return articleRevisionRepository.save(revision);
    }

    /**
     * Convenience helper to save snapshot by article id.
     */
    @Transactional
    public ArticleRevision saveSnapshotByArticleId(Long articleId, User revisedBy, String note) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", articleId));
        return saveSnapshot(article, revisedBy, note);
    }

    /**
     * Restore article content/title from a specific revision.
     * Creates a new revision snapshot before applying the restore.
     * Returns the updated article.
     */
    @Transactional
    public Article restoreFromRevision(Long revisionId, User restoredBy) {
        ArticleRevision revision = articleRevisionRepository.findById(revisionId)
                .orElseThrow(() -> new ResourceNotFoundException("ArticleRevision", revisionId));
        Article article = revision.getArticle();
        
        // Save current state before restoring
        saveSnapshot(article, restoredBy, "Before restore from revision " + revisionId);
        
        article.setTitle(revision.getTitle());
        article.setContent(revision.getContent());
        return articleRepository.save(article);
    }
}

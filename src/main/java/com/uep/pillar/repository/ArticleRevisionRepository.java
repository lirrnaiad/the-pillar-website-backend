package com.uep.pillar.repository;

import com.uep.pillar.model.ArticleRevision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ArticleRevision entity operations.
 */
@Repository
public interface ArticleRevisionRepository extends JpaRepository<ArticleRevision, Long> {

    /**
     * Find all revisions for an article, ordered by revision date descending.
     * 
     * @param articleId the article ID
     * @return list of revisions (newest first)
     */
    @Query("SELECT r FROM ArticleRevision r WHERE r.article.id = :articleId ORDER BY r.revisedAt DESC")
    List<ArticleRevision> findByArticleId(@Param("articleId") Long articleId);

    /**
     * Find all revisions for an article with pagination.
     * 
     * @param articleId the article ID
     * @param pageable pagination info
     * @return paginated list of revisions
     */
    Page<ArticleRevision> findByArticleIdOrderByRevisedAtDesc(Long articleId, Pageable pageable);

    /**
     * Find the most recent revision for an article.
     * 
     * @param articleId the article ID
     * @param pageable pagination info (use PageRequest.of(0, 1) to get the latest)
     * @return page containing the latest revision if any
     */
    @Query("SELECT r FROM ArticleRevision r WHERE r.article.id = :articleId ORDER BY r.revisedAt DESC")
    Page<ArticleRevision> findLatestByArticleId(@Param("articleId") Long articleId, Pageable pageable);

    /**
     * Find revisions made by a specific user.
     * 
     * @param userId the user ID who made the revisions
     * @param pageable pagination info
     * @return paginated list of revisions
     */
    Page<ArticleRevision> findByRevisedByIdOrderByRevisedAtDesc(Long userId, Pageable pageable);

    /**
     * Count revisions for an article.
     * 
     * @param articleId the article ID
     * @return number of revisions
     */
    long countByArticleId(Long articleId);

    /**
     * Delete all revisions for an article.
     * Note: This is usually handled by ON DELETE CASCADE in the database,
     * but provided here for explicit cleanup if needed.
     * 
     * @param articleId the article ID
     */
    @Modifying
    void deleteByArticleId(Long articleId);
}


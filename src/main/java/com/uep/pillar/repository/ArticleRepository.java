package com.uep.pillar.repository;

import com.uep.pillar.model.Article;
import com.uep.pillar.model.enums.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Article entity operations.
 * 
 * Note: Due to @SQLRestriction on Article entity, all queries automatically
 * exclude soft-deleted articles (where deleted_at IS NOT NULL).
 * Use native queries to include deleted articles if needed.
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // ==================== FIND BY SLUG ====================

    /**
     * Find an article by its URL slug.
     * 
     * @param slug the article slug
     * @return the article if found (excludes soft-deleted)
     */
    Optional<Article> findBySlug(String slug);

    /**
     * Check if an article with the given slug exists.
     * 
     * @param slug the slug to check
     * @return true if exists
     */
    boolean existsBySlug(String slug);

    // ==================== FIND BY STATUS ====================

    /**
     * Find articles by status.
     * 
     * @param status the article status
     * @param pageable pagination info
     * @return paginated list of articles
     */
    Page<Article> findByStatus(ArticleStatus status, Pageable pageable);

    /**
     * Find all published articles.
     * 
     * @param pageable pagination info
     * @return paginated list of published articles
     */
    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC")
    Page<Article> findAllPublished(Pageable pageable);

    // ==================== FIND BY CATEGORY ====================

    /**
     * Find articles by category ID.
     * 
     * @param categoryId the category ID
     * @param pageable pagination info
     * @return paginated list of articles
     */
    Page<Article> findByCategoryId(Integer categoryId, Pageable pageable);

    /**
     * Find published articles by category slug.
     * 
     * @param categorySlug the category slug
     * @param pageable pagination info
     * @return paginated list of published articles in that category
     */
    @Query("SELECT a FROM Article a WHERE a.category.slug = :categorySlug AND a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC")
    Page<Article> findPublishedByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);

    // ==================== FIND BY AUTHOR ====================

    /**
     * Find articles by author ID.
     * 
     * @param authorId the author's user ID
     * @param pageable pagination info
     * @return paginated list of articles
     */
    Page<Article> findByAuthorId(Long authorId, Pageable pageable);

    /**
     * Find published articles by author ID.
     * 
     * @param authorId the author's user ID
     * @param pageable pagination info
     * @return paginated list of published articles
     */
    @Query("SELECT a FROM Article a WHERE a.author.id = :authorId AND a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC")
    Page<Article> findPublishedByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    // ==================== FEATURED ARTICLES ====================

    /**
     * Find featured articles.
     * 
     * @param pageable pagination info
     * @return paginated list of featured articles
     */
    @Query("SELECT a FROM Article a WHERE a.featured = true AND a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC")
    Page<Article> findFeatured(Pageable pageable);

    /**
     * Find top N featured articles.
     * 
     * @param limit maximum number of results
     * @return list of featured articles
     */
    @Query("SELECT a FROM Article a WHERE a.featured = true AND a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC LIMIT :limit")
    List<Article> findTopFeatured(@Param("limit") int limit);

    // ==================== FULL-TEXT SEARCH ====================

    /**
     * Search articles using PostgreSQL full-text search.
     * Searches in title, excerpt, and content with ranking.
     * 
     * @param searchQuery the search query
     * @param pageable pagination info
     * @return paginated list of matching articles, ranked by relevance
     */
    @Query(value = """
        SELECT * FROM articles 
        WHERE status = 'PUBLISHED' 
        AND deleted_at IS NULL
        AND search_vector @@ plainto_tsquery('english', :searchQuery)
        ORDER BY ts_rank(search_vector, plainto_tsquery('english', :searchQuery)) DESC
        """, 
        countQuery = """
        SELECT COUNT(*) FROM articles 
        WHERE status = 'PUBLISHED' 
        AND deleted_at IS NULL
        AND search_vector @@ plainto_tsquery('english', :searchQuery)
        """,
        nativeQuery = true)
    Page<Article> search(@Param("searchQuery") String searchQuery, Pageable pageable);

    /**
     * Search articles with highlighted snippets.
     * Returns article IDs and headline snippets for display.
     * 
     * @param searchQuery the search query
     * @param limit maximum results
     * @return list of Object arrays: [id, title, headline_snippet]
     */
    @Query(value = """
        SELECT id, title, 
               ts_headline('english', content, plainto_tsquery('english', :searchQuery), 
                          'StartSel=<mark>, StopSel=</mark>, MaxWords=35, MinWords=15') as snippet
        FROM articles 
        WHERE status = 'PUBLISHED' 
        AND deleted_at IS NULL
        AND search_vector @@ plainto_tsquery('english', :searchQuery)
        ORDER BY ts_rank(search_vector, plainto_tsquery('english', :searchQuery)) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> searchWithHighlights(@Param("searchQuery") String searchQuery, @Param("limit") int limit);

    // ==================== PUBLICATION ISSUE ====================

    /**
     * Find articles by publication issue.
     * 
     * @param issueId the publication issue ID
     * @param pageable pagination info
     * @return paginated list of articles
     */
    Page<Article> findByIssueId(Long issueId, Pageable pageable);

    // ==================== TAGS ====================

    /**
     * Find published articles by tag slug.
     * 
     * @param tagSlug the tag slug
     * @param pageable pagination info
     * @return paginated list of articles with that tag
     */
    @Query("SELECT a FROM Article a JOIN a.tags t WHERE t.slug = :tagSlug AND a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC")
    Page<Article> findPublishedByTagSlug(@Param("tagSlug") String tagSlug, Pageable pageable);

    // ==================== SOFT DELETE & RESTORE ====================

    /**
     * Soft delete an article by setting deleted_at timestamp.
     * 
     * @param articleId the article ID
     * @param deletedAt the deletion timestamp
     */
    @Modifying
    @Query("UPDATE Article a SET a.deletedAt = :deletedAt WHERE a.id = :articleId")
    void softDelete(@Param("articleId") Long articleId, @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * Restore a soft-deleted article.
     * Uses native query to bypass @SQLRestriction filter.
     * 
     * @param articleId the article ID
     */
    @Modifying
    @Query(value = "UPDATE articles SET deleted_at = NULL WHERE id = :articleId", nativeQuery = true)
    void restore(@Param("articleId") Long articleId);

    /**
     * Find article by ID including soft-deleted.
     * Uses native query to bypass @SQLRestriction filter.
     * 
     * @param articleId the article ID
     * @return the article if found
     */
    @Query(value = "SELECT * FROM articles WHERE id = :articleId", nativeQuery = true)
    Optional<Article> findByIdIncludingDeleted(@Param("articleId") Long articleId);

    // ==================== VIEW COUNT ====================

    /**
     * Increment the view count for an article.
     * 
     * @param articleId the article ID
     */
    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :articleId")
    void incrementViewCount(@Param("articleId") Long articleId);

    // ==================== STATISTICS ====================

    /**
     * Count articles by status.
     * 
     * @param status the status to count
     * @return count of articles with that status
     */
    long countByStatus(ArticleStatus status);

    /**
     * Count articles by category.
     * 
     * @param categoryId the category ID
     * @return count of articles in that category
     */
    long countByCategoryId(Integer categoryId);

    /**
     * Find recent published articles.
     * 
     * @param limit maximum results
     * @return list of recent articles
     */
    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC LIMIT :limit")
    List<Article> findRecentPublished(@Param("limit") int limit);

    /**
     * Find most viewed published articles.
     * 
     * @param limit maximum results
     * @return list of popular articles
     */
    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' ORDER BY a.viewCount DESC LIMIT :limit")
    List<Article> findMostViewed(@Param("limit") int limit);
}


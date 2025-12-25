package com.uep.pillar.repository;

import com.uep.pillar.model.PublicationIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PublicationIssue entity operations.
 */
@Repository
public interface PublicationIssueRepository extends JpaRepository<PublicationIssue, Long> {

    /**
     * Find a publication issue by its URL slug.
     * 
     * @param slug the issue slug
     * @return the issue if found
     */
    Optional<PublicationIssue> findBySlug(String slug);

    /**
     * Find all published issues (where published_at is not null).
     * Ordered by publication date descending (newest first).
     * 
     * @param pageable pagination info
     * @return paginated list of published issues
     */
    @Query("SELECT pi FROM PublicationIssue pi WHERE pi.publishedAt IS NOT NULL ORDER BY pi.publishedAt DESC")
    Page<PublicationIssue> findAllPublished(Pageable pageable);

    /**
     * Find all published issues without pagination.
     * 
     * @return list of all published issues
     */
    @Query("SELECT pi FROM PublicationIssue pi WHERE pi.publishedAt IS NOT NULL ORDER BY pi.publishedAt DESC")
    List<PublicationIssue> findAllPublished();

    /**
     * Check if an issue with the given slug exists.
     * 
     * @param slug the slug to check
     * @return true if exists
     */
    boolean existsBySlug(String slug);

    /**
     * Find the most recent published issue.
     * 
     * @return the latest issue if any
     */
    @Query("SELECT pi FROM PublicationIssue pi WHERE pi.publishedAt IS NOT NULL ORDER BY pi.publishedAt DESC LIMIT 1")
    Optional<PublicationIssue> findLatestPublished();
}


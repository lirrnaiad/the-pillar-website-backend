package com.uep.pillar.model.enums;

/**
 * Enum representing the workflow status of an article.
 * 
 * Flow: DRAFT → PENDING_REVIEW → PUBLISHED → ARCHIVED
 *                    ↓
 *               REJECTED (with feedback)
 */
public enum ArticleStatus {
    DRAFT,           // Initial state, only author can see
    PENDING_REVIEW,  // Submitted for editor review
    PUBLISHED,       // Live on the website
    ARCHIVED,        // Hidden but preserved
    REJECTED         // Returned with feedback
}


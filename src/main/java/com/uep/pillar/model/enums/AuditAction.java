package com.uep.pillar.model.enums;

/**
 * Enum representing the type of action logged in the audit trail.
 */
public enum AuditAction {
    CREATE,     // Entity created
    UPDATE,     // Entity updated
    DELETE,     // Entity deleted (soft delete)
    PUBLISH,    // Article published
    ARCHIVE,    // Article archived
    RESTORE,    // Entity restored from soft delete
    LOGIN,      // User logged in
    LOGOUT      // User logged out
}


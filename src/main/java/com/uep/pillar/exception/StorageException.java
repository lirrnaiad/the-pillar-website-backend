package com.uep.pillar.exception;

/**
 * Exception representing storage-related errors (e.g., Cloudinary operations).
 */
public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

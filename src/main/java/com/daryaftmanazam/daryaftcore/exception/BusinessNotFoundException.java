package com.daryaftmanazam.daryaftcore.exception;

/**
 * Exception thrown when a business is not found.
 */
public class BusinessNotFoundException extends RuntimeException {
    
    public BusinessNotFoundException(Long id) {
        super("Business not found with id: " + id);
    }
    
    public BusinessNotFoundException(String message) {
        super(message);
    }
}

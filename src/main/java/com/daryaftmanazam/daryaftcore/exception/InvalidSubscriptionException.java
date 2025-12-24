package com.daryaftmanazam.daryaftcore.exception;

/**
 * Exception thrown when a subscription operation is invalid.
 */
public class InvalidSubscriptionException extends RuntimeException {
    
    public InvalidSubscriptionException(String message) {
        super(message);
    }
}

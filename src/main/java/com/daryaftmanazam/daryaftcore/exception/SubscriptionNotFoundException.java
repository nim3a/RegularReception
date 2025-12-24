package com.daryaftmanazam.daryaftcore.exception;

/**
 * Exception thrown when a subscription is not found.
 */
public class SubscriptionNotFoundException extends RuntimeException {
    
    public SubscriptionNotFoundException(Long id) {
        super("Subscription not found with id: " + id);
    }
    
    public SubscriptionNotFoundException(String message) {
        super(message);
    }
}

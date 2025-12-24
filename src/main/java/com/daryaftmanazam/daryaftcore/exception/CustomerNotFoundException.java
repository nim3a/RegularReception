package com.daryaftmanazam.daryaftcore.exception;

/**
 * Exception thrown when a customer is not found.
 */
public class CustomerNotFoundException extends RuntimeException {
    
    public CustomerNotFoundException(Long id) {
        super("Customer not found with id: " + id);
    }
    
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
